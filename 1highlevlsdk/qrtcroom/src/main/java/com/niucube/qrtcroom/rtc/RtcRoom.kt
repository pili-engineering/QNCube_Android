package com.niucube.qrtcroom.rtc

import android.content.Context
import android.util.Log
import android.view.View
import com.niucube.absroom.*
import com.niucube.absroom.seat.UserExtension
import com.niucube.qrtcroom.adminTrack.AdminTrackManager
import com.niucube.qrtcroom.mixstream.MixStreamManager
import com.niucube.qrtcroom.screencapture.ScreenShareManager
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomManager
import com.niucube.comproom.provideMeId
import com.niucube.qrtcroom.customtrack.CustomTrackShareManager
import com.niucube.qrtcroom.qplayer.QMediaPlayer
import com.niucube.qrtcroom.qplayer.QPlayerEventListener
import com.niucube.qrtcroom.qplayer.QPlayerRenderView
import com.niucube.rtm.*
import com.qiniu.droid.rtc.*
import com.qiniu.jsonutil.JsonUtils
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class RtcRoom(
    val appContext: Context,
    val mQNRTCSetting: QNRTCSetting = QNRTCSetting(),
    private val mClientConfig: QNRTCClientConfig = QNRTCClientConfig(
        QNClientMode.LIVE,
        QNClientRole.BROADCASTER
    )
) {
    private val TAG = "RtcRoom"

    companion object {
        const val TAG_CAMERA = "camera"

        //屏幕采集轨道的标记
        const val TAG_SCREEN = "screen"
        const val TAG_AUDIO = "audio"
    }


    internal val rtcEventWrap: QNRTCEngineEventWrap
            by lazy {
                QNRTCEngineEventWrap().apply {
                    addSelfQNRTCEngineEventWrap(this)
                }
            }
    internal val mRTCUserStore = QRTCUserStore()
    private val mQNRTCEventListener = QNRTCEventListener { }

    init {
        QNRTC.init(appContext, mQNRTCSetting, mQNRTCEventListener)
    }

    val rtcClient by lazy {

        QNRTC.createClient(mClientConfig, rtcEventWrap).apply {
            setAutoSubscribe(false)
        }
    }

    val localVideoTrack: QNCameraVideoTrack? get() = mRTCUserStore.localVideoTrack
    val localAudioTrack: QNMicrophoneAudioTrack? get() = mRTCUserStore.localAudioTrack

    private val mQPlayerEventListenerWarp = QPlayerEventListenerWarp()
    protected val mMediaPlayer by lazy {
        QMediaPlayer(appContext).apply {
            setEventListener(mQPlayerEventListenerWarp)
        }
    }
    open var mClientRole: ClientRoleType = com.niucube.comproom.ClientRoleType.CLIENT_ROLE_PULLER
    protected fun ClientRoleType.toQNClientRoleType(): QNClientRole {
        return if (this == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
            QNClientRole.BROADCASTER
        } else {
            QNClientRole.AUDIENCE
        }
    }

    protected suspend fun setClientRoleSuspend(value: ClientRoleType) =
        suspendCoroutine<Unit> { continuation ->
            setClientRole(value, object : QNClientRoleResultCallback {
                override fun onResult(p0: QNClientRole?) {
                    continuation.resume(Unit)
                }

                override fun onError(p0: Int, p1: String) {
                    if (p0 == 24007) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(RtcException(p0, p1))
                    }
                }
            })
        }


    protected open fun setClientRole(value: ClientRoleType, call: QNClientRoleResultCallback) {
        val doWorkCall = {
            //主播变观众
            if ((mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER || mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) &&
                (value == ClientRoleType.CLIENT_ROLE_PULLER)
            ) {
                mMediaPlayer.start()

            } else {
                if (mClientRole == ClientRoleType.CLIENT_ROLE_PULLER &&
                    ((value == ClientRoleType.CLIENT_ROLE_BROADCASTER) || (value == ClientRoleType.CLIENT_ROLE_AUDIENCE))
                ) {
                    mMediaPlayer.stop()

                }
            }
            mClientRole = value
        }
        mRTCUserStore.rtcUsers.forEach {
            it.setPreviewVisibility(
                if (value == ClientRoleType.CLIENT_ROLE_PULLER) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
            )
        }

        val startTime = System.currentTimeMillis()
        rtcClient.setClientRole(
            value.toQNClientRoleType(), object : QNClientRoleResultCallback {
                override fun onResult(p0: QNClientRole?) {
                    val duration = System.currentTimeMillis() - startTime
                    Log.d("sitDown", " setClientRole duration ${duration} ")
                    doWorkCall.invoke()
                    call.onResult(p0)
                }

                override fun onError(p0: Int, p1: String?) {
                    Log.d("sitDown", " setClientRole onError ${p0} ")
                    if (24001 == p0 || 24007 == p0) {
                        doWorkCall.invoke()
                        Log.d("sitDown", " doWorkCall.invoke()")
                        call.onResult(value.toQNClientRoleType())
                        Log.d("sitDown", " onResult.invoke()")
                    } else {
                        call.onError(p0, p1)
                    }
                }
            })
    }

    /**
     *  添加你需要的引擎状回调
     */
    fun addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: ExtQNClientEventListener) {
        rtcEventWrap.addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener)
    }

    private fun addExtraQNRTCEngineEventListenerToHead(extraQNRTCEngineEventListener: ExtQNClientEventListener) {
        rtcEventWrap.addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener, true)
    }

    /**
     * 移除额外的监听
     */
    fun removeExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: ExtQNClientEventListener) {
        rtcEventWrap.removeExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener)
    }

    fun setPullRender(renderView: QPlayerRenderView) {
        mMediaPlayer.setPlayerRenderView(renderView)
    }

    fun addPlayerEventListener(playerEventListener: QPlayerEventListener) {
        mQPlayerEventListenerWarp.addEventListener(playerEventListener)
    }

    fun removePlayerEventListener(playerEventListener: QPlayerEventListener) {
        mQPlayerEventListenerWarp.removeEventListener(playerEventListener)
    }

    //屏幕共享工具
    val screenShareManager by lazy { ScreenShareManager(this) }

    //自带轨道工具
    val customTrackShareManager by lazy { CustomTrackShareManager() }

    //服务端管理员轨道工具
    val adminTrackManager by lazy { AdminTrackManager(this) }

    //混流器
    val mixStreamManager by lazy { MixStreamManager(this) }

    //默认视频参数
    private var mQNVideoEncoderConfig = QNVideoEncoderConfig(480, 640, 15, 1000)

    //默认音频参数
    private var mQNMicrophoneAudioTrackConfig = QNMicrophoneAudioTrackConfig()

    private var mQNRTCEngineEventListener: QNClientEventListener = object : SimpleQNRTCListener {
        override fun onUserJoined(p0: String, p1: String?) {
            super.onUserJoined(p0, p1)
            mRTCUserStore.addUser(p0)
        }

        override fun onConnectionStateChanged(
            state: QNConnectionState,
            p1: QNConnectionDisconnectedInfo?
        ) {
            if (state == QNConnectionState.CONNECTED) {
                mRTCUserStore.addUser(mRTCUserStore.joinRoomParams.meId)
                val tracks = ArrayList<QNLocalTrack>()
                // 成功加入房间
                // 提前启用的音视频模块 加入成功后发布/启动
                if (isVideoEnable && isAudioEnable) {
                    tracks.add(localAudioTrack!!)
                    tracks.add(localVideoTrack!!)
                } else {
                    if (isAudioEnable) {
                        tracks.add(localAudioTrack!!)
                    }
                    if (isVideoEnable) {
                        tracks.add(localVideoTrack!!)
                    }
                }
                rtcClient.publish(object : QNPublishResultCallback {
                    override fun onPublished() {
                        GlobalScope.launch(Dispatchers.Main) {
                            rtcEventWrap.onLocalPublished(
                                mRTCUserStore.joinRoomParams.meId ?: "", tracks
                            )
                        }
                    }

                    override fun onError(p0: Int, p1: String?) {
                        Log.d("publish", "onError ${p0} ${p1}")
                    }
                }, tracks)
            }
        }

        override fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>) {
            super.onLocalUnpublished(var1, var2)
            var2.forEach {
                mRTCUserStore.removeUserTrack(var1, it)
            }
        }

        override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
            super.onLocalPublished(var1, var2)
            var2.forEach {
                mRTCUserStore.setUserTrack(var1, it)
            }
        }

        override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
            super.onUserPublished(p0, p1)
            p1.forEach {
                if (!p0.startsWith("admin-publisher")) {
                    rtcClient.subscribe(it)
                }
                mRTCUserStore.setUserTrack(p0, it)
            }
        }

        override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
            super.onUserUnpublished(p0, p1)
            p1.forEach {
                mRTCUserStore.removeUserTrack(p0, it)
            }
        }

        override fun onUserLeft(p0: String) {
            super.onUserLeft(p0)
            mRTCUserStore.clearUser(p0)
        }
    }

    protected open fun addSelfQNRTCEngineEventWrap(eventWrap: QNRTCEngineEventWrap) {
        eventWrap.addExtraQNRTCEngineEventListener(mQNRTCEngineEventListener)
    }

    /**
     * 设置本地预览窗口
     */
    fun setLocalCameraWindowView(view: QNTextureView) {
        if (localVideoTrack == null) {
            createVideoTrack(mQNVideoEncoderConfig)
        }
        mRTCUserStore.setLocalCameraPreView(view)
    }

    /**
     * 设置某人的摄像头预览窗口 可以在任何时候调用
     */
    fun setUserCameraWindowView(uid: String, view: QNTextureView) {
        mRTCUserStore.setUserCameraPreView(uid, view)
    }

    protected fun clear() {
        mClientRole = ClientRoleType.CLIENT_ROLE_AUDIENCE
        mRTCUserStore.clear()
    }

    private fun createVideoTrack(params: QNVideoEncoderConfig): QNCameraVideoTrack {
        // 创建本地 Camera 视频 Track
        val cameraVideoTrackConfig = QNCameraVideoTrackConfig(TAG_CAMERA)
            .setVideoEncoderConfig(
                params
            )
        return QNRTC.createCameraVideoTrack(cameraVideoTrackConfig);

    }

    private fun createAudioTrack(microphoneAudioTrackConfig: QNMicrophoneAudioTrackConfig): QNMicrophoneAudioTrack {
        return QNRTC.createMicrophoneAudioTrack(QNMicrophoneAudioTrackConfig(TAG_AUDIO).apply {
            audioQuality = microphoneAudioTrackConfig.audioQuality
            isCommunicationModeOn = microphoneAudioTrackConfig.isCommunicationModeOn
        })
    }

    protected open fun setCameraVideoTrackParams(videoEncoderConfig: VideoTrackParams) {
        mQNVideoEncoderConfig = QNVideoEncoderConfig(
            videoEncoderConfig.width,
            videoEncoderConfig.height,
            videoEncoderConfig.fps,
            videoEncoderConfig.bitrate
        )
    }

    protected open fun setMicrophoneAudioParams(microphoneAudioTrackConfig: AudioTrackParams) {
        mQNMicrophoneAudioTrackConfig = QNMicrophoneAudioTrackConfig(TAG_AUDIO).apply {
            isCommunicationModeOn = microphoneAudioTrackConfig.communicationModeOn
        }
    }

    protected var isVideoEnable = false

    protected open fun createVideoTrack() {
        if (localVideoTrack == null) {
            mRTCUserStore.localVideoTrack = createVideoTrack(mQNVideoEncoderConfig)
        }
    }

    protected open fun enableVideo(call: QNPublishResultCallback) {
        if (localVideoTrack == null) {
            mRTCUserStore.localVideoTrack = createVideoTrack(mQNVideoEncoderConfig)
        }
        if (com.niucube.comproom.RoomManager.mCurrentRoom?.isJoined == true) {
            rtcClient.publish(object : QNPublishResultCallback {
                override fun onPublished() {
                    GlobalScope.launch(Dispatchers.Main) {
                        rtcEventWrap.onLocalPublished(
                            RoomManager.mCurrentRoom?.provideMeId() ?: "", listOf(localVideoTrack!!)
                        )
                        call.onPublished()
                    }
                }

                override fun onError(p0: Int, p1: String?) {
                    GlobalScope.launch(Dispatchers.Main) {
                        call.onError(p0, p1)
                    }
                }
            }, listOf(localVideoTrack))
        } else {
            call.onPublished()
            isVideoEnable = true
        }
    }

    protected suspend fun suspendEnableVideo() = suspendCoroutine<Unit> { continuation ->
        enableVideo(object : QNPublishResultCallback {
            override fun onPublished() {
                continuation.resume(Unit)
            }

            override fun onError(p0: Int, p1: String) {
                continuation.resumeWithException(RtcException(p0, p1))
            }
        })
    }

    protected var isAudioEnable = false
    protected open fun createAudioTrack() {
        if (localAudioTrack == null) {
            mRTCUserStore.localAudioTrack = createAudioTrack(mQNMicrophoneAudioTrackConfig)
        }
    }

    protected open fun enableAudio(call: QNPublishResultCallback) {
        if (localAudioTrack == null) {
            mRTCUserStore.localAudioTrack = createAudioTrack(mQNMicrophoneAudioTrackConfig)
        }
        if (com.niucube.comproom.RoomManager.mCurrentRoom?.isJoined == true) {
            rtcClient.publish(object : QNPublishResultCallback {
                override fun onPublished() {
                    GlobalScope.launch(Dispatchers.Main) {
                        rtcEventWrap.onLocalPublished(
                            RoomManager.mCurrentRoom?.provideMeId() ?: "", listOf(localAudioTrack!!)
                        )
                        call.onPublished()
                    }
                }

                override fun onError(p0: Int, p1: String?) {
                    GlobalScope.launch(Dispatchers.Main) {
                        call.onError(p0, p1)
                    }
                }
            }, listOf(localAudioTrack))
        } else {
            call.onPublished()
            isAudioEnable = true
        }
    }

    protected suspend fun suspendEnableAudio() = suspendCoroutine<Unit> { continuation ->
        enableAudio(object : QNPublishResultCallback {
            override fun onPublished() {
                continuation.resume(Unit)
            }

            override fun onError(p0: Int, p1: String) {
                continuation.resumeWithException(RtcException(p0, p1))
            }
        })
    }

    /**
     * 关闭视频模块
     */
    protected open fun disableVideo() {
        if (com.niucube.comproom.RoomManager.mCurrentRoom?.isJoined == true) {
            localVideoTrack?.let {
                mRTCUserStore.removeUserTrack(mRTCUserStore.joinRoomParams.meId, it)
            }
            localVideoTrack?.destroy()
            mRTCUserStore.localVideoTrack = null
        }
        isVideoEnable = false
    }

    protected fun leaveRtc() {
        rtcClient.leave()
        mRTCUserStore.clearUser(mRTCUserStore.joinRoomParams.meId)
        mRTCUserStore.clearAllTrack()
    }

    /**
     * 关闭音频模块
     */
    protected open fun disableAudio() {
        if (com.niucube.comproom.RoomManager.mCurrentRoom?.isJoined == true) {
            localAudioTrack?.let {
                mRTCUserStore.removeUserTrack(mRTCUserStore.joinRoomParams.meId, it)
            }
            localAudioTrack?.destroy()
            mRTCUserStore.localAudioTrack = null
        }
        isAudioEnable = false
    }

    /**
     * 切换摄像头
     */
    open fun switchCamera(call: QNCameraSwitchResultCallback? = null) {
        localVideoTrack?.switchCamera(object : QNCameraSwitchResultCallback {
            override fun onSwitched(p0: Boolean) {
                call?.onSwitched(p0)
            }

            override fun onError(p0: String?) {
                call?.onError(p0)
            }
        })
    }

    /**
     * 禁用本地视频推流
     */
    open fun muteLocalVideo(muted: Boolean) {
        localVideoTrack?.isMuted = muted
    }

    /**
     * 禁用本地音频推流
     */
    open fun muteLocalAudio(muted: Boolean) {
        localAudioTrack?.isMuted = muted
    }

    /**
     * 获取某人的视频轨道 如果需要用到track
     */
    fun getUserVideoTrackInfo(uid: String): QNTrack? {
        return mRTCUserStore.findUser(uid)?.cameraTrack?.track
    }

    /**
     * 获取某人的音频轨道
     */
    fun getUserAudioTrackInfo(uid: String): QNTrack? {
        return mRTCUserStore.findUser(uid)?.microphoneTrack?.track
    }

    /**
     * 离开房间
     */
    protected open suspend fun leaveRoom() {
        try {
            RtmManager.rtmClient.leaveChannel(
                com.niucube.comproom.RoomManager.mCurrentRoom?.provideImGroupId() ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        RoomManager.dispatchRoomLeaving()
        clear()
        rtcClient.leave()
    }

    protected open suspend fun joinRoom(
        roomEntity: com.niucube.comproom.RoomEntity,
        userExt: UserExtension?
    ) {
        mixStreamManager.init(roomEntity.provideRoomId(), roomEntity.providePushUri())
        mRTCUserStore.joinRoomParams = QJoinRoomParams(
            roomEntity.provideRoomToken(),
            userExt.toString(),
            roomEntity.provideImGroupId(),
            roomEntity.providePullUri()
        )
        mRTCUserStore.addUser(mRTCUserStore.joinRoomParams.meId)
        mMediaPlayer.setUp(roomEntity.providePullUri())
        com.niucube.comproom.RoomManager.dispatchRoomEntering(roomEntity)
        //加入im房间
        RtmManager.rtmClient.joinChannel(roomEntity.provideImGroupId())

        //拉流角色绑定播放器
        if (mClientRole == ClientRoleType.CLIENT_ROLE_PULLER) {
            mMediaPlayer.start()
        } else {
            //主播角色/观众角色 加入房间
            val msg = if (userExt != null) {
                JsonUtils.toJson(userExt)
            } else ""
            //加入rtc房间
            joinRtc(
                roomEntity.provideRoomToken(),
                msg
            )
        }
        //分发房间进入
        com.niucube.comproom.RoomManager.dispatchRoomJoined(roomEntity)
    }

    /**
     * 销毁房间
     */
    open fun closeRoom() {
        com.niucube.comproom.RoomManager.dispatchCloseRoom()
        rtcClient?.leave()
        // 反初始化
        QNRTC.deinit()
        clear()
        mMediaPlayer.release()
        mRTCUserStore.closeCallDispatcher.close()
    }
}