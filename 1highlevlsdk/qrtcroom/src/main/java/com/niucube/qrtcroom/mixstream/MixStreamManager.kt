package com.niucube.qrtcroom.mixstream

import com.niucube.qrtcroom.rtc.RtcRoom
import com.niucube.qrtcroom.rtc.QRTCUserStore
import com.niucube.qrtcroom.liblog.QLiveLogUtil
import com.niucube.qrtcroom.rtc.SimpleQNRTCListener
import com.qiniu.droid.rtc.*
import kotlinx.coroutines.*

enum class MixType(var isStart: Boolean, var id: String = "") {
    mix(false), forward(false)
}

class MixStreamManager(val mQRtcLiveRoom: RtcRoom) {

    private var streamId = ""
    private var pushUrl = ""
    private var serialnum = 1
    private val mEngine by lazy { mQRtcLiveRoom.rtcClient }

    //混流任务
    private var mQNMergeJob: QNTranscodingLiveStreamingConfig? = null

    //单路转推任务
    private var mQNForwardJob: QNDirectLiveStreamingConfig? = null
    private var mMixStreamParams: QNTranscodingLiveStreamingConfig? = null
    var mMixType = MixType.forward
        private set
    var isInit = false
        private set

    private var mRestartJob: SchedulerJob? = null
    private fun checkRestartJob() {
        if (mRestartJob != null) {
            return
        }
        mRestartJob = SchedulerJob(2500) {
            if (mMixType.isStart) {
                return@SchedulerJob
            }
            QLiveLogUtil.d(
                "MixStreamHelperImp",
                "MixStreamHelperImp timeOut ${mMixType.name} "
            )
            if (mMixType == MixType.forward) {
                createForwardJob()
                mQNForwardJob?.let {
                    mEngine.startLiveStreaming(it)
                }
                return@SchedulerJob
            }
            if (mMixType == MixType.mix) {
                mQNMergeJob = if (mMixStreamParams != null) {
                    createMergeOptions(mMixStreamParams!!)
                } else {
                    throw Exception("NO QNTranscodingLiveStreamingConfig")
                }
                mQNMergeJob?.let {
                    mEngine.startLiveStreaming(it)
                }
                return@SchedulerJob
            }
        }
        mRestartJob?.start()
    }

    internal fun init(
        streamId: String,
        pushUrl: String,
    ) {
        isInit = true
        this.pushUrl = pushUrl
        this.streamId = streamId
        mEngine.setLiveStreamingListener(object : QNLiveStreamingListener {
            override fun onStarted(streamID: String) {
                QLiveLogUtil.d(
                    "MixStreamHelperImp",
                    "MixStreamHelperImp onStarted ${mMixType.name}"
                )
                // 转推任务创建成功时触发此回调
                mRestartJob?.cancel()
                mRestartJob = null

                mMixType.isStart = true
                if (mMixType == MixType.forward) {
                    stopMixStreamJob()
                }

                if (mMixType == MixType.mix) {
                    stopForwardJob()
                    commitOpt()
                }
            }

            override fun onStopped(streamID: String) {
                QLiveLogUtil.d("MixStreamHelperImp", " onStopped  ${streamID}")
                if (mMixType.id != streamID) {
                    return
                }
                mMixType.isStart = false
                // 转推任务成功停止时触发此回调

            }

            override fun onTranscodingTracksUpdated(streamID: String) {
                // 合流布局更新成功时触发此回调
                QLiveLogUtil.d("MixStreamHelperImp", " onTranscodingTracksUpdated  ${streamID}")
            }

            override fun onError(streamID: String, errorInfo: QNLiveStreamingErrorInfo) {
                // 转推任务出错时触发此回调
                QLiveLogUtil.d(
                    "MixStreamHelperImp",
                    "MixStreamHelperImp onError  ${mMixType.name}" + errorInfo.message + "  " + errorInfo.code
                )
                if (mMixType.id != streamID) {
                    return
                }
                mMixType.isStart = false
                mRestartJob?.cancel()
                mRestartJob = null
                checkRestartJob()
            }
        })
        mQRtcLiveRoom.addExtraQNRTCEngineEventListener(object : SimpleQNRTCListener {
            override fun onUserLeft(p0: String) {
                if (mQRtcLiveRoom.mRTCUserStore.joinRoomParams.meId == p0) {
                    mQNForwardJob = null
                    mQNMergeJob = null
                    mRestartJob?.cancel()
                }
            }

            override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
                super.onLocalPublished(var1, var2)
                QLiveLogUtil.d("MixStreamHelperImp", "onLocalPublished ${var1}")
                val user = mQRtcLiveRoom.mRTCUserStore.findUser(var1) ?: return
                var2.forEach {
                    if (user.cameraTrack.track?.trackID == it.trackID) {
                        if (user.cameraTrack.mergeOptionWithOutTrackID != null) {
                            commitOpt()
                        }
                    }
                    if (user.microphoneTrack.track?.trackID == it.trackID) {
                        if (user.microphoneTrack.mergeOptionWithOutTrackID != null) {
                            commitOpt()
                        }
                    }
                }
            }

            override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserPublished(p0, p1)
                val user = mQRtcLiveRoom.mRTCUserStore.findUser(p0) ?: return
                p1.forEach {
                    if (user.cameraTrack.track?.trackID == it.trackID) {
                        if (user.cameraTrack.mergeOptionWithOutTrackID != null) {
                            commitOpt()
                        }
                    }
                    if (user.microphoneTrack.track?.trackID == it.trackID) {
                        if (user.microphoneTrack.mergeOptionWithOutTrackID != null) {
                            commitOpt()
                        }
                    }
                }
            }
        })
        mQRtcLiveRoom.mRTCUserStore.closeCallDispatcher.addCloseObserver(object :
            QRTCUserStore.CloseObserver {
            override fun close() {
                mRestartJob?.cancel()
            }
        })
    }

    private fun checkUrl(pushUrl: String): String {
        return if (pushUrl.contains("?")) {
            pushUrl + "&serialnum=${serialnum++}"; // 设置合流任务的推流地址
        } else {
            pushUrl + "?serialnum=${serialnum++}"; // 设置合流任务的推流地址
        }
    }

    private fun createMergeOptions(mixStreamParams: QNTranscodingLiveStreamingConfig): QNTranscodingLiveStreamingConfig {
        mixStreamParams.streamID =
            streamId + "?serialnum=${serialnum++}";// 设置 stream id，该 id 为合流任务的唯一标识符
        mixStreamParams.url = checkUrl(pushUrl)
        return mixStreamParams
    }

    //创建前台转推
    private fun createForwardJob() {
        val mDirectLiveStreamingConfig = QNDirectLiveStreamingConfig()
        mDirectLiveStreamingConfig.streamID = streamId + "?serialnum=${serialnum++}"
        mDirectLiveStreamingConfig.url = checkUrl(pushUrl)
        QLiveLogUtil.d("MixStreamHelperImp", "createForwardJob ${mDirectLiveStreamingConfig.url}")
        mQRtcLiveRoom.mRTCUserStore.localAudioTrack?.let {
            mDirectLiveStreamingConfig.audioTrack = it
        }
        mQRtcLiveRoom.mRTCUserStore.localVideoTrack?.let {
            mDirectLiveStreamingConfig.videoTrack = it
        }
        mQNForwardJob = mDirectLiveStreamingConfig
        QLiveLogUtil.d("MixStreamHelperImp", "createForwardJob ")
    }

    // private val restartJob =
    /**
     * 启动前台转推 默认实现推本地轨道
     */
    fun startForwardJob() {
        mMixType = MixType.forward
        mMixType.isStart = false
        createForwardJob()
        mMixType.id = mQNForwardJob!!.streamID
        QLiveLogUtil.d("MixStreamHelperImp", "startForwardJob ")
        checkRestartJob()
        mEngine.startLiveStreaming(mQNForwardJob);
    }

    /**
     * 停止前台推流
     */
    private fun stopForwardJob() {
        QLiveLogUtil.d("MixStreamHelperImp", "stopForwardJob ")
        mQNForwardJob?.let {
            mEngine.stopLiveStreaming(mQNForwardJob)
        }
        mQNForwardJob = null
    }

    /**
     * 开始混流转推
     */
    fun startMixStreamJob(mixStreamParamsWithOutSteamIdAndUrl: QNTranscodingLiveStreamingConfig) {
        mMixStreamParams = mixStreamParamsWithOutSteamIdAndUrl
        mMixType = MixType.mix
        mMixType.isStart = false
        QLiveLogUtil.d("MixStreamHelperImp", "startMixStreamJob ")
        clear()
        mQNMergeJob = createMergeOptions(mixStreamParamsWithOutSteamIdAndUrl)
        mMixType.id = mQNMergeJob!!.streamID
        checkRestartJob()
        mEngine.startLiveStreaming(mQNMergeJob)
    }

    private fun clear() {
        QLiveLogUtil.d("MixStreamHelperImp", "clear ")
        mQRtcLiveRoom.mRTCUserStore.clearTrackMergeOption()
    }

    private fun stopMixStreamJob() {
        QLiveLogUtil.d("MixStreamHelperImp", "stopMixStreamJob ")
        mQNMergeJob?.let {
            mEngine.stopLiveStreaming(it)
        }
        mQNMergeJob = null
    }

    fun updateUserVideoMergeOptions(
        uid: String,
        optionWithOutTrackID: QNTranscodingLiveStreamingTrack?, commitNow: Boolean
    ) {
        val user = mQRtcLiveRoom.mRTCUserStore.findUser(uid) ?: return
        val cTrack = user.cameraTrack.track
        QLiveLogUtil.d("MixStreamHelperImp", "updateUserVideoMergeOptions ${uid}")
        user.cameraTrack.mergeOptionWithOutTrackID = optionWithOutTrackID
        if (cTrack == null) {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserVideoMergeOptions ${uid} track没找到")
        } else {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserVideoMergeOptions ${uid} track找到了")
            if (optionWithOutTrackID != null) {
                // tracksMap[trackId] = option
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(
                    mQNMergeJob!!.streamID,
                    listOf(QNTranscodingLiveStreamingTrack().apply {
                        this.trackID = cTrack.trackID
                    })
                )
            }
            if (commitNow) {
                commitOpt()
            }
        }
    }

    fun updateUserScreenMergeOptions(
        uid: String,
        optionWithOutTrackID: QNTranscodingLiveStreamingTrack?, commitNow: Boolean
    ) {
        val user = mQRtcLiveRoom.mRTCUserStore.findUser(uid) ?: return
        val cTrack = user.screenTrack.track
        QLiveLogUtil.d("MixStreamHelperImp", "updateUserVideoMergeOptions ${uid}")
        user.screenTrack.mergeOptionWithOutTrackID = optionWithOutTrackID
        if (cTrack == null) {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserVideoMergeOptions ${uid} track没找到")
        } else {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserVideoMergeOptions ${uid} track找到了")
            if (optionWithOutTrackID != null) {
                // tracksMap[trackId] = option
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(
                    mQNMergeJob!!.streamID,
                    listOf(QNTranscodingLiveStreamingTrack().apply {
                        this.trackID = cTrack.trackID
                    })
                )
            }
            if (commitNow) {
                commitOpt()
            }
        }
    }

    fun updateUserAudioMergeOptions(
        uid: String,
        optionWithOutTrackID: QNTranscodingLiveStreamingTrack?, commitNow: Boolean
    ) {
        val user = mQRtcLiveRoom.mRTCUserStore.findUser(uid) ?: return
        val aTrack = user.microphoneTrack.track
        QLiveLogUtil.d("MixStreamHelperImp", "updateUserAudioMergeOptions ${uid}")
        user.microphoneTrack.mergeOptionWithOutTrackID = optionWithOutTrackID
        if (aTrack == null) {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserAudioMergeOptions ${uid} track没找到")
        } else {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserAudioMergeOptions ${uid} track找到了")
            if (optionWithOutTrackID != null) {
                // tracksMap[trackId] = op
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(
                    mQNMergeJob!!.streamID,
                    listOf(QNTranscodingLiveStreamingTrack().apply {
                        this.trackID = aTrack.trackID
                    })
                )
            }
            if (commitNow) {
                commitOpt()
            }
        }
    }

    private fun String.checkEmpty(call:(it:String)->Unit){
        if(this.isNotEmpty()){
            call.invoke(this)
        }
    }
    fun commitOpt() {
        val mMergeTrackOptions = ArrayList<QNTranscodingLiveStreamingTrack>()
        mQRtcLiveRoom.mRTCUserStore.rtcUsers.forEach {
            it.cameraTrack.mergeOptionWithOutTrackID?.let { op ->
                it.cameraTrack.track?.trackID?.checkEmpty {
                    op.trackID = it
                    mMergeTrackOptions.add(op)
                }
            }
            it.microphoneTrack.mergeOptionWithOutTrackID?.let { op ->
                it.microphoneTrack.track?.trackID?.checkEmpty {
                    op.trackID = it
                    mMergeTrackOptions.add(op)
                }
            }
            it.screenTrack.mergeOptionWithOutTrackID?.let { op ->
                it.screenTrack.track?.trackID?.checkEmpty {
                    op.trackID = it
                    mMergeTrackOptions.add(op)
                }
            }
        }
        QLiveLogUtil.d("MixStreamHelperImp", "commitOpt size ${mMergeTrackOptions.size} ")
        if (mMixType != MixType.forward && mMixType.isStart) {
            val id = if (mMixType == MixType.mix) {
                mQNMergeJob?.streamID ?: ""
            } else {
                return
            }
            mMergeTrackOptions.forEach {
                QLiveLogUtil.d("MixStreamHelperImp", "commitOpt ${it.toJsonObject().toString()} ")
            }
            if(mMergeTrackOptions.isEmpty()){
                return
            }
            mEngine.setTranscodingLiveStreamingTracks(
                id,
                mMergeTrackOptions
            )
        }
    }

    class SchedulerJob(
        private val delayTimeMillis: Long,
        private val coroutineScope: CoroutineScope = GlobalScope,
        val action: suspend CoroutineScope.() -> Unit
    ) {
        private var job: Job? = null
        fun start() {
            job = coroutineScope.launch(Dispatchers.Main) {
                try {
                    while (true) {
                        delay(delayTimeMillis)
                        action()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun cancel() {
            job?.cancel()
            job = null
        }
    }
}