package com.niucube.basemutableroom.mixstream

import android.text.TextUtils
import android.util.Log
import com.niucube.comproom.RoomManager
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.niucube.basemutableroom.RtcRoom
import com.niucube.basemutableroom.customtrack.CustomTrackShareManager
import com.niucube.basemutableroom.screencapture.ScreenShareManager
import com.qiniu.droid.rtc.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MixStreamHelperImp(
    private val rtcRoom: RtcRoom,
    private val mEngine: QNRTCClient,
    private val localVideoTrack: QNLocalVideoTrack?,
    private val localAudioTrack: QNLocalAudioTrack?,
    private val screenShareManager: ScreenShareManager,
    private val customTrackShareManager: CustomTrackShareManager
) : MixStreamManager, com.niucube.comproom.RoomLifecycleMonitor {

    private var mMixStreamParams: MixStreamManager.MixStreamParams? = null
    public val tracksMap = HashMap<String, MixStreamManager.MergeTrackOption>()
    private val toDoAudioMergeOptionsMap = HashMap<String, MixStreamManager.MergeTrackOption>()
    private val toDoVideoMergeOptionsMap = HashMap<String, MixStreamManager.MergeTrackOption>()
    private val toDoScreenMergeOptionsMap = HashMap<String, MixStreamManager.MergeTrackOption>()
    private val toDoCustomMergeOptionsMap = HashMap<String, MixStreamManager.MergeTrackOption>()

    private var isAutoMixAllAudio = false

    init {
        mEngine.setLiveStreamingListener(object : QNLiveStreamingListener {
            override fun onStarted(streamID: String) {
                Log.d("MixStreamHelperImp","onStarted ${streamID}")
                // 转推任务创建成功时触发此回调
                if (mQNMergeJob != null) {
                    isMixStreamIng = true
                    commitOpt()
                }
                if (mQNForwardJob != null) {

                }
            }

            override fun onStopped(streamID: String) {
                Log.d("MixStreamHelperImp","onStopped ${streamID}")
                // 转推任务成功停止时触发此回调
            }

            override fun onTranscodingTracksUpdated(streamID: String) {
                // 合流布局更新成功时触发此回调
                Log.d("MixStreamHelperImp","onTranscodingTracksUpdated ${streamID}")
            }

            override fun onError(streamID: String, errorInfo: QNLiveStreamingErrorInfo) {
                // 转推任务出错时触发此回调
                Log.d("MixStreamHelperImp","MixStreamHelperImp onError"+errorInfo.message)
            }
        })
        rtcRoom.addExtraQNRTCEngineEventListener(object : SimpleQNRTCListener {

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
                        RtcRoom.TAG_AUDIO -> {

                            if (isAutoMixAllAudio) {
                                tracksMap[track.trackID] = MixStreamManager.MergeTrackOption()
                            }

                            toDoAudioMergeOptionsMap[p0]?.let {
                                tracksMap[track.trackID] = it
                                commitOpt()
                            }

                            toDoAudioMergeOptionsMap.remove(p0)
                        }
                        RtcRoom.TAG_CAMERA -> {
                            toDoVideoMergeOptionsMap[p0]?.let {
                                tracksMap[track.trackID] = it
                                commitOpt()
                            }
                            toDoVideoMergeOptionsMap.remove(p0)
                        }
                        RtcRoom.TAG_SCREEN -> {
                            toDoScreenMergeOptionsMap[p0]?.let {
                                tracksMap[track.trackID] = it
                                commitOpt()
                            }
                            toDoScreenMergeOptionsMap[p0]
                        }
                        else -> {
                            toDoCustomMergeOptionsMap["${p0}${track.tag}"]?.let {
                                tracksMap[track.trackID] = it
                                commitOpt()
                            }
                            toDoCustomMergeOptionsMap.remove("${p0}${track.tag}")
                        }
                    }
                }
            }
        })
    }

    //混流任务
    private var mQNMergeJob: QNTranscodingLiveStreamingConfig? = null

    private fun createMergeJob() {
        mQNMergeJob = QNTranscodingLiveStreamingConfig().apply {
            setStreamID(com.niucube.comproom.RoomManager.mCurrentRoom?.provideRoomId()); // 设置 stream id，该 id 为合流任务的唯一标识符
            setUrl(com.niucube.comproom.RoomManager.mCurrentRoom?.providePushUri()); // 设置合流任务的推流地址
            setWidth(mMixStreamParams!!.mixStreamWidth); // 设置合流画布的宽度
            setHeight(mMixStreamParams!!.mixStringHeight); // 设置合流画布的高度
            setVideoFrameRate(mMixStreamParams!!.fps); // 设置合流任务的视频帧率
//           setRenderMode(QNRenderMode.ASPECT_FILL); // 设置合流任务的默认画面填充方式
            setBitrate(mMixStreamParams!!.mixBitrate); // 设置合流任务的码率，单位: kbps
            mMixStreamParams!!.qnBackGround?.let { setBackground(it) }
            mMixStreamParams?.watermarks?.let {
                setWatermarks(it);
            }
        }
    }


    /**
     *  重写前台推流 处理屏幕采集场景
     */
    private var mQNForwardJob: QNDirectLiveStreamingConfig? = null

    //创建前台转推
    private fun createForwardJob() {

        mQNForwardJob = QNDirectLiveStreamingConfig().apply {
            val mDirectLiveStreamingConfig = QNDirectLiveStreamingConfig()
            mDirectLiveStreamingConfig.streamID = RoomManager.mCurrentRoom?.provideRoomId() ?: ""
            mDirectLiveStreamingConfig.url = RoomManager.mCurrentRoom?.providePushUri()
            localAudioTrack?.let {
                mDirectLiveStreamingConfig.audioTrack = it
            }
            localVideoTrack?.let {
                mDirectLiveStreamingConfig.videoTrack = it
            }
        }
    }

    private var isMixStreamIng = false
    private var isForwardJob = false

    /**
     * 启动前台转推 默认实现推本地轨道
     */
    override fun startForwardJob() {
        if (isMixStreamIng) {
            stopMixStreamJob()
        }
        if (mQNForwardJob == null) {
            createForwardJob()
        }

        mEngine.startLiveStreaming(mQNForwardJob);
    }

    /**
     * 停止前台推流
     */
    override fun stopForwardJob() {
        mQNForwardJob?.let {
            mEngine.stopLiveStreaming(mQNForwardJob)
        }
        mQNForwardJob = null
        isForwardJob = false
    }

    /**
     * 开始混流转推
     */
    override fun startMixStreamJob() {
        if (isForwardJob) {
            stopForwardJob()
        }
        if (mQNMergeJob == null) {
            createMergeJob()
        }
        mEngine.startLiveStreaming(mQNMergeJob)
    }

    override fun stopMixStreamJob() {
        if (!TextUtils.isEmpty(mQNMergeJob?.streamID)) {
            mEngine.stopLiveStreaming(mQNMergeJob)
        }
        mQNMergeJob = null
        isMixStreamIng = false
    }

    /**
     * 自动混房间所有音频
     */
    override fun startAutoMixAllAudio() {
        isAutoMixAllAudio = true
    }

    override fun setMixParams(params: MixStreamManager.MixStreamParams) {
        mMixStreamParams = params
    }

    override fun updateUserVideoMergeOptions(
        uid: String,
        option: MixStreamManager.MergeTrackOption?, commitNow:Boolean
    ) {
        val trackId = rtcRoom.getUserVideoTrackInfo(uid)?.trackID
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
                mEngine.removeTranscodingLiveStreamingTracks(mQNMergeJob!!.streamID, listOf(QNTranscodingLiveStreamingTrack().apply {
                    this.trackID = trackId
                }))
                tracksMap.remove(trackId)
            }
            if(commitNow){
                commitOpt()
            }
        }
    }

    override fun updateUserAudioMergeOptions(
        uid: String,
        isNeed: Boolean,commitNow:Boolean
    ) {
        val trackId = rtcRoom.getUserAudioTrackInfo(uid)?.trackID

        if (trackId == null) {
            if (isNeed) {
                toDoAudioMergeOptionsMap[uid] = MixStreamManager.MergeTrackOption()
            } else {
                toDoAudioMergeOptionsMap.remove(uid)
            }
        } else {
            if (isNeed) {
                tracksMap[trackId] = MixStreamManager.MergeTrackOption()
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(mQNMergeJob!!.streamID, listOf(QNTranscodingLiveStreamingTrack().apply {
                    this.trackID = trackId
                }))
                tracksMap.remove(trackId)
            }
            if(commitNow){
                commitOpt()
            }
        }
    }

    override fun updateVideoMergeOptions(
        trackID: String,
        option: MixStreamManager.MergeTrackOption?, commitNow: Boolean
    ) {
        if (option != null) {
            tracksMap[trackID] = option
        } else {
            mEngine.removeTranscodingLiveStreamingTracks(mQNMergeJob!!.streamID, listOf(QNTranscodingLiveStreamingTrack().apply {
                this.trackID = trackID
            }))

            tracksMap.remove(trackID)

        }
        if(commitNow){
            commitOpt()
        }
    }

    override fun updateAudioMergeOptions(trackID: String, isNeed: Boolean,commitNow: Boolean) {
        if (isNeed) {
            tracksMap[trackID] = MixStreamManager.MergeTrackOption()
        } else {
            mEngine.removeTranscodingLiveStreamingTracks(mQNMergeJob!!.streamID, listOf(QNTranscodingLiveStreamingTrack().apply {
                this.trackID = trackID
            }))
            tracksMap.remove(trackID)
        }
        if(commitNow){
            commitOpt()
        }
    }

    override fun updateUserScreenMergeOptions(
        uid: String,
        option: MixStreamManager.MergeTrackOption?, commitNow: Boolean
    ) {
        val trackId = screenShareManager.getUserScreenTrackInfo(uid)?.trackID
        if (trackId == null) {
            if (option != null) {
                toDoScreenMergeOptionsMap[uid] = option
            } else {
                toDoScreenMergeOptionsMap.remove(uid)
            }
        } else {
            if (option != null) {
                tracksMap[trackId] = option
                if(commitNow){
                    commitOpt()
                }
            } else {
                tracksMap.remove(trackId)
                mEngine.removeTranscodingLiveStreamingTracks(mQNMergeJob!!.streamID, listOf(QNTranscodingLiveStreamingTrack().apply {
                    trackID = trackId
                }))
            }
        }
    }

    override fun updateUserCustomVideoMergeOptions(
        extraTrackTag: String,
        uid: String,
        option: MixStreamManager.MergeTrackOption?, commitNow: Boolean
    ) {
        val trackId = customTrackShareManager.getUserExtraTrackInfo(extraTrackTag, uid)?.trackID
        if (trackId == null) {
            if (option != null) {
                toDoCustomMergeOptionsMap["${uid}${extraTrackTag}"] = option
            } else {
                toDoCustomMergeOptionsMap.remove("${uid}${extraTrackTag}")
            }
        } else {
            if (option != null) {
                tracksMap[trackId] = option
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(mQNMergeJob!!.streamID, listOf(QNTranscodingLiveStreamingTrack().apply {
                    this.trackID = trackId
                }))
                tracksMap.remove(trackId)
            }
            if(commitNow){
                commitOpt()
            }
        }
    }

    override fun updateUserCustomAudioMergeOptions(
        extraTrackTag: String,
        uid: String,
        isNeed: Boolean,commitNow: Boolean
    ) {
        val trackId = customTrackShareManager.getUserExtraTrackInfo(extraTrackTag, uid)?.trackID
        if (trackId == null) {
            if (isNeed) {
                toDoCustomMergeOptionsMap["${uid}${extraTrackTag}"] =
                    MixStreamManager.MergeTrackOption()
            } else {
                toDoCustomMergeOptionsMap.remove("${uid}${extraTrackTag}")
            }
        } else {
            if (isNeed) {
                tracksMap[trackId] = MixStreamManager.MergeTrackOption()
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(mQNMergeJob!!.streamID, listOf(QNTranscodingLiveStreamingTrack().apply {
                    this.trackID = trackId
                }))
                tracksMap.remove(trackId)
            }
            if(commitNow){
                commitOpt()
            }
        }
    }

     override fun commitOpt() {
        if (isForwardJob || isMixStreamIng) {
            val mMergeTrackOptions = ArrayList<QNTranscodingLiveStreamingTrack>()
            tracksMap.entries.forEach {
                val key = it.key
                val op = it.value
                mMergeTrackOptions.add(QNTranscodingLiveStreamingTrack().apply {
                    trackID = key
                    x = op.mX
                    y = op.mY
                    zOrder = op.mZ
                    width = op.mWidth
                    height = op.mHeight
                    renderMode = op.mStretchMode?:QNRenderMode.ASPECT_FILL
                })
            }
            mEngine.setTranscodingLiveStreamingTracks(mQNMergeJob!!.streamID, mMergeTrackOptions)
        }
    }

    override fun onRoomLeft(roomEntity: com.niucube.comproom.RoomEntity?) {
        mQNForwardJob = null
        tracksMap.clear()
    }
}