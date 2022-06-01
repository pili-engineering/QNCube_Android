package com.nucube.rtclive

import android.text.TextUtils
import android.util.Log
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.qiniu.droid.rtc.*

class MixStreamManager(val mRtcLiveRoom: RtcLiveRoom) {

    private var localVideoTrack: QNLocalVideoTrack? = null
    private var localAudioTrack: QNLocalAudioTrack? = null

    var mMixStreamParams: MixStreamParams? = null
        private set
    private val tracksMap = HashMap<String, TrackMergeOption>()
    private val toDoAudioMergeOptionsMap = HashMap<String, TrackMergeOption>()
    private val toDoVideoMergeOptionsMap = HashMap<String, TrackMergeOption>()
    private var streamId = ""
    private var pushUrl = ""
    private var serialnum = 1
    private val mEngine by lazy { mRtcLiveRoom.mClient }

    //混流任务
    var mQNMergeJob: QNTranscodingLiveStreamingConfig? = null
        private set
    var mQNForwardJob: QNDirectLiveStreamingConfig? = null
        private set

    //房间人数
    var roomUser = 0
        private set

    var isInit = false
    fun setTrack(videoTrack: QNLocalVideoTrack?, audioTrack: QNLocalAudioTrack?) {
        this.localVideoTrack = videoTrack
        this.localAudioTrack = audioTrack
    }

    fun init(streamId: String, pushUrl: String, mixStreamParams: MixStreamParams) {
        isInit = true
        this.mMixStreamParams = mixStreamParams
        this.pushUrl = pushUrl
        this.streamId = streamId
        mEngine.setLiveStreamingListener(object : QNLiveStreamingListener {
            override fun onStarted(streamID: String) {
                Log.d("MixStreamHelperImp", "MixStreamHelperImp onStarted ${isMixStreamIng}")
                // 转推任务创建成功时触发此回调
                if (mQNMergeJob != null) {
                    isMixStreamIng = true
                    commitOpt()
                } else {
                    isMixStreamIng = false
                }
                isForwardJob = mQNForwardJob != null

            }

            override fun onStopped(streamID: String) {
                // 转推任务成功停止时触发此回调
            }

            override fun onTranscodingTracksUpdated(streamID: String) {
                // 合流布局更新成功时触发此回调
            }

            override fun onError(streamID: String, errorInfo: QNLiveStreamingErrorInfo) {
                // 转推任务出错时触发此回调
                Log.d(
                    "MixStreamHelperImp",
                    "MixStreamHelperImp onError" + errorInfo.message + "  " + errorInfo.code
                )
            }
        })
        mRtcLiveRoom.addExtraQNRTCEngineEventListenerToHead(object : SimpleQNRTCListener {
            override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserPublished(p0, p1)
                onPublished(p0, p1)
            }

            override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
                super.onLocalPublished(var1, var2)
                onPublished(var1, var2)
            }

            override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserUnpublished(p0, p1)
                p1.forEach {
                    tracksMap.remove(it.trackID)
                }
            }

            override fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>) {
                super.onLocalUnpublished(var1, var2)
                var2.forEach {
                    tracksMap.remove(it.trackID)
                }
            }

            private fun onPublished(p0: String, p1: List<QNTrack>) {
                p1.forEach { track ->
                    when (track.tag) {
                        RtcLiveRoom.TAG_AUDIO -> {
                            toDoAudioMergeOptionsMap[p0]?.let {
                                tracksMap[track.trackID] = it
                                commitOpt()
                            }
                            toDoAudioMergeOptionsMap.remove(p0)
                        }

                        RtcLiveRoom.TAG_CAMERA -> {
                            toDoVideoMergeOptionsMap[p0]?.let {
                                tracksMap[track.trackID] = it
                                commitOpt()
                            }
                            toDoVideoMergeOptionsMap.remove(p0)
                        }
                    }
                }
            }

            override fun onUserJoined(p0: String, p1: String?) {
                super.onUserJoined(p0, p1)
                roomUser++
            }

            override fun onUserLeft(p0: String) {
                roomUser--
                if (mRtcLiveRoom.meId == p0) {
                    onRoomLeft()
                }
            }
        })


    }

    private fun createMergeJob() {
        Log.d("MixStreamHelperImp", "createMergeJob ")
        mQNMergeJob = QNTranscodingLiveStreamingConfig().apply {
            streamID = streamId; // 设置 stream id，该 id 为合流任务的唯一标识符
            url = pushUrl + "?serialnum=${serialnum++}"; // 设置合流任务的推流地址
            Log.d("MixStreamHelperImp", "createMergeJob${url} ")
            width = mMixStreamParams!!.mixStreamWidth; // 设置合流画布的宽度
            height = mMixStreamParams!!.mixStringHeight; // 设置合流画布的高度
            videoFrameRate = mMixStreamParams!!.fps; // 设置合流任务的视频帧率
//           setRenderMode(QNRenderMode.ASPECT_FILL); // 设置合流任务的默认画面填充方式
            bitrate = mMixStreamParams!!.mixBitrate; // 设置合流任务的码率，单位: kbps
            mMixStreamParams!!.qnBackGround?.let { background = it }
            mMixStreamParams?.watermarks?.let {
                watermarks = it;
            }
        }
    }

    //创建前台转推
    private fun createForwardJob() {

        val mDirectLiveStreamingConfig = QNDirectLiveStreamingConfig()
        mDirectLiveStreamingConfig.streamID = streamId
        mDirectLiveStreamingConfig.url = pushUrl + "?serialnum=${serialnum++}"
        Log.d("MixStreamHelperImp", "createForwardJob ${mDirectLiveStreamingConfig.url}")
        localAudioTrack?.let {
            mDirectLiveStreamingConfig.audioTrack = it
        }
        localVideoTrack?.let {
            mDirectLiveStreamingConfig.videoTrack = it
        }
        mQNForwardJob = mDirectLiveStreamingConfig

        Log.d("MixStreamHelperImp", "createForwardJob ")
    }

    private var isMixStreamIng = false
    private var isForwardJob = false


    /**
     * 启动前台转推 默认实现推本地轨道
     */
    fun startForwardJob() {
        if (mQNMergeJob != null) {
            stopMixStreamJob()
        }
        if (mQNForwardJob == null) {
            createForwardJob()
        }
        Log.d("MixStreamHelperImp", "startForwardJob ")
        mEngine.startLiveStreaming(mQNForwardJob);
    }

    /**
     * 停止前台推流
     */
    fun stopForwardJob() {
        Log.d("MixStreamHelperImp", "stopForwardJob ")
        mQNForwardJob?.let {
            mEngine.stopLiveStreaming(mQNForwardJob)
        }
        mQNForwardJob = null
        isForwardJob = false
    }

    /**
     * 开始混流转推
     */
    fun startMixStreamJob() {
        Log.d("MixStreamHelperImp", "startMixStreamJob ")
        isMixStreamIng = false
        if (mQNForwardJob != null) {
            stopForwardJob()
        }
        if (mQNMergeJob == null) {
            createMergeJob()
        }
        mEngine.startLiveStreaming(mQNMergeJob)
    }

    /**
     * 启动新的混流任务
     */
    fun startNewMixStreamJob(mixStreamParams: MixStreamParams) {
        Log.d("MixStreamHelperImp", "startNewMixStreamJob ")
        if (mQNForwardJob != null) {
            stopForwardJob()
        }
        if (mQNMergeJob != null) {
            stopMixStreamJob()
        }
        mQNMergeJob = QNTranscodingLiveStreamingConfig().apply {
            streamID = streamId; // 设置 stream id，该 id 为合流任务的唯一标识符
            url = pushUrl + "?serialnum=${serialnum++}"; // 设置合流任务的推流地址

            Log.d("MixStreamHelperImp", "startNewMixStreamJob${url} ")

            width = mixStreamParams.mixStreamWidth; // 设置合流画布的宽度
            height = mixStreamParams.mixStringHeight; // 设置合流画布的高度
            videoFrameRate = mixStreamParams.fps; // 设置合流任务的视频帧率
//           setRenderMode(QNRenderMode.ASPECT_FILL); // 设置合流任务的默认画面填充方式
            bitrate = mixStreamParams.mixBitrate; // 设置合流任务的码率，单位: kbps
            mixStreamParams.qnBackGround?.let { background = it }
            mixStreamParams.watermarks?.let {
                watermarks = it;
            }
        }
        mEngine.startLiveStreaming(mQNMergeJob);
    }


    fun stopMixStreamJob() {
        Log.d("MixStreamHelperImp", "stopMixStreamJob ")
        tracksMap.clear()
        toDoAudioMergeOptionsMap.clear()
        toDoVideoMergeOptionsMap.clear()

        if (!TextUtils.isEmpty(mQNMergeJob?.streamID)) {
            mEngine.stopLiveStreaming(mQNMergeJob)
        }
        mQNMergeJob = null
        isMixStreamIng = false

    }

    fun updateUserVideoMergeOptions(
        uid: String,
        option: CameraMergeOption?, commitNow: Boolean
    ) {
        val trackId = mRtcLiveRoom.getUserVideoTrackInfo(uid)?.trackID
        if (trackId == null) {
            if (option != null) {
                toDoVideoMergeOptionsMap[uid] = option
            } else {
                toDoVideoMergeOptionsMap.remove(uid)
            }
        } else {
            if (option != null) {
                tracksMap[trackId] = option
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(
                    mQNMergeJob!!.streamID,
                    listOf(QNTranscodingLiveStreamingTrack().apply {
                        this.trackID = trackId
                    })
                )
                tracksMap.remove(trackId)
            }
            if (commitNow) {
                commitOpt()
            }
        }
    }

    fun updateUserAudioMergeOptions(
        uid: String,
        op: MicrophoneMergeOption, commitNow: Boolean
    ) {
        val trackId = mRtcLiveRoom.getUserAudioTrackInfo(uid)?.trackID
        if (trackId == null) {
            if (op.isNeed) {
                toDoAudioMergeOptionsMap[uid] = op
            } else {
                toDoAudioMergeOptionsMap.remove(uid)
            }
        } else {
            if (op.isNeed) {
                tracksMap[trackId] = op
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(
                    mQNMergeJob!!.streamID,
                    listOf(QNTranscodingLiveStreamingTrack().apply {
                        this.trackID = trackId
                    })
                )
                tracksMap.remove(trackId)
            }
            if (commitNow) {
                commitOpt()
            }
        }
    }

    fun commitOpt() {

        if (isMixStreamIng) {
            val mMergeTrackOptions = ArrayList<QNTranscodingLiveStreamingTrack>()
            tracksMap.entries.forEach {
                val key = it.key
                val op = it.value
                if (op is CameraMergeOption) {
                    mMergeTrackOptions.add(QNTranscodingLiveStreamingTrack().apply {
                        trackID = key
                        x = op.mX
                        y = op.mY
                        zOrder = op.mZ
                        width = op.mWidth
                        height = op.mHeight
                        renderMode = op.mStretchMode
                    })
                }
                if (op is MicrophoneMergeOption) {
                    mMergeTrackOptions.add(QNTranscodingLiveStreamingTrack().apply {
                        trackID = key
                    })
                }

                Log.d("MixStreamHelperImp",
                    "commitOpt fab发布混流参数  " + mMergeTrackOptions.get(mMergeTrackOptions.size - 1)
                        .toJsonObject().toString()
                )
            }
            mEngine.setTranscodingLiveStreamingTracks(mQNMergeJob!!.streamID, mMergeTrackOptions)
        }
    }

    private fun onRoomLeft() {
        mQNForwardJob = null
        mQNMergeJob = null
        tracksMap.clear()
        toDoAudioMergeOptionsMap.clear()
        toDoVideoMergeOptionsMap.clear()
    }
}