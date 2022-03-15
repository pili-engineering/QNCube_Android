package com.niucube.rtcroom

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.niucube.absroom.*
import com.niucube.absroom.seat.UserExtension
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomManager
import com.niucube.comproom.provideMeId
import com.niucube.qnrtcsdk.QNRTCEngineEventWrap
import com.niucube.qnrtcsdk.RtcEngineWrap
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.niucube.rtcroom.adminTrack.AdminTrackManager
import com.niucube.rtcroom.customtrack.CustomTrackShareManagerImp
import com.niucube.rtcroom.mixstream.MixStreamHelperImp
import com.niucube.rtcroom.mixstream.MixStreamManager
import com.niucube.rtcroom.screencapture.ScreenShareManagerImp
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
    val mClientConfig: QNRTCClientConfig = QNRTCClientConfig(
        QNClientMode.LIVE,
        QNClientRole.BROADCASTER
    )
) : RtcEngineWrap(appContext, mQNRTCSetting, mClientConfig) {
    private val TAG = "RtcRoom"

    companion object {
        const val TAG_CAMERA = "camera"

        //屏幕采集轨道的标记
        const val TAG_SCREEN = "screen"
        const val TAG_AUDIO = "audio"
    }

    open var mClientRole: com.niucube.comproom.ClientRoleType =
        com.niucube.comproom.ClientRoleType.CLIENT_ROLE_PULLER

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
                    continuation.resumeWithException(RtcException(p0, p1))
                }
            })
        }

    protected fun setClientRoleForce(value: ClientRoleType) {
        val doWorkCall = {
            //主播变观众
            if ((mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER || mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) &&
                (value == ClientRoleType.CLIENT_ROLE_PULLER)
            ) {
                RoomManager.mCurrentRoom?.let {
                    mIAudiencePlayerView?.startAudiencePlay(it)
                }
            } else
                if (mClientRole == ClientRoleType.CLIENT_ROLE_PULLER &&
                    ((value == ClientRoleType.CLIENT_ROLE_BROADCASTER) || (value == ClientRoleType.CLIENT_ROLE_AUDIENCE))
                ) {
                    mIAudiencePlayerView?.stopAudiencePlay()
                }
            mClientRole = value
        }
        if (value != ClientRoleType.CLIENT_ROLE_PULLER) {
            mClient.setClientRole(
                value.toQNClientRoleType(), object : QNClientRoleResultCallback {
                    override fun onResult(p0: QNClientRole?) {}
                    override fun onError(p0: Int, p1: String?) {}
                })
            doWorkCall.invoke()
        } else {
            doWorkCall.invoke()
        }
    }

    open protected fun setClientRole(value: ClientRoleType, call: QNClientRoleResultCallback) {
        val doWorkCall = {
            //主播变观众
            if ((mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER || mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) &&
                (value == ClientRoleType.CLIENT_ROLE_PULLER)
            ) {
                RoomManager.mCurrentRoom?.let {
                    mIAudiencePlayerView?.startAudiencePlay(it)
                }
            } else
                if (mClientRole == ClientRoleType.CLIENT_ROLE_PULLER &&
                    ((value == ClientRoleType.CLIENT_ROLE_BROADCASTER) || (value == ClientRoleType.CLIENT_ROLE_AUDIENCE))
                ) {
                    mIAudiencePlayerView?.stopAudiencePlay()
                }
            mClientRole = value
        }
        if (value != ClientRoleType.CLIENT_ROLE_PULLER) {
            val startTime = System.currentTimeMillis()
            mClient.setClientRole(
                value.toQNClientRoleType(), object : QNClientRoleResultCallback {
                    override fun onResult(p0: QNClientRole?) {
                        val duration = System.currentTimeMillis() - startTime
                        Log.d("setClientRole"," setClientRole duration ${duration} ")
                        doWorkCall.invoke()
                        call.onResult(p0)
                    }

                    override fun onError(p0: Int, p1: String?) {
                        if (24001 == p0) {
                            doWorkCall.invoke()
                            call.onResult(value.toQNClientRoleType())
                        } else {
                            call.onError(p0, p1)
                        }
                    }
                })
        } else {
            doWorkCall.invoke()
            call.onResult(value.toQNClientRoleType())
        }
    }

    //房间里存在的所以轨道
    protected var mAllTrack = ArrayList<QNTrack>()

    /**
     * 本地视频轨道 初始值使用默认参数
     */
    protected var localVideoTrack: QNCameraVideoTrack? = null

    /**
     * 本地音频轨道 初始值默认音频参数
     */
    protected var localAudioTrack: QNMicrophoneAudioTrack? = null
    protected open val mRtcRoomSignalingLister: RtcRoomSignalingLister by lazy {
        RtcRoomSignalingLister(
            this
        )
    }

    //麦位信令
    protected open var mRtcRoomSignaling = RtcRoomSignaling()

    //用户提前设置的摄像头窗口 ，还没有轨道绑定，稍后对方轨道发布后绑定
    private val mUserUnbindCameraWindowMap = HashMap<String, QNTextureView>()

    //  private val mUserBindCameraWindowMap = HashMap<QNTrackInfo, QNTextureView>()
    protected var mIAudiencePlayerView: IAudiencePlayerView? = null

    //设置观众模式的拉流播放器，sdk根据当前角色模式切换订阅播放/拉流播放
    fun setAudiencePlayerView(playerView: IAudiencePlayerView) {
        mIAudiencePlayerView = playerView
        if (RoomManager.mCurrentRoom?.isJoined == true && mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
            mIAudiencePlayerView?.startAudiencePlay(RoomManager.mCurrentRoom!!)
        }
    }

    fun getIAudiencePlayerView(): IAudiencePlayerView? {
        return mIAudiencePlayerView
    }

    private val mIAudienceJoinListeners = ArrayList<IAudienceJoinListener>()

    //用户角色进入退出监听
    fun addIAudienceJoinListener(listener: IAudienceJoinListener) {
        mIAudienceJoinListeners.add(listener)
    }

    //移除用户角色监听
    fun removeIAudienceJoinListener(listener: IAudienceJoinListener) {
        mIAudienceJoinListeners.remove(listener)
    }

    //屏幕共享工具
    val screenShareManager by lazy { ScreenShareManagerImp(this) }

    //自带轨道工具
    val customTrackShareManager by lazy { CustomTrackShareManagerImp(this) }

    //服务端管理员轨道工具
    val adminTrackManager by lazy { AdminTrackManager(this) }

    //混流器
    private var mMixStreamHelper: MixStreamManager? = null

    //默认视频参数
    private var mQNVideoEncoderConfig = QNVideoEncoderConfig(480, 640, 15, 1000)

    //默认音频参数
    private var mQNMicrophoneAudioTrackConfig = QNMicrophoneAudioTrackConfig()

    private var mQNRTCEngineEventListener: QNClientEventListener = object : SimpleQNRTCListener {
        override fun onConnectionStateChanged(
            state: QNConnectionState,
            p1: QNConnectionDisconnectedInfo?
        ) {
            if (state == QNConnectionState.CONNECTED) {
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
                mClient.publish(object : QNPublishResultCallback {
                    override fun onPublished() {
                        GlobalScope.launch(Dispatchers.Main) {
                            mQNRTCEngineEventWrap.onLocalPublished(
                                RoomManager.mCurrentRoom?.provideMeId() ?: "", tracks
                            )
                        }
                    }

                    override fun onError(p0: Int, p1: String?) {
                        Log.d("publish", "onError ${p0} ${p1}")
                    }
                }, tracks)
            }
        }

        override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
            afterPublished(var1, var2, false)
        }

        override fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>) {
            var2.forEach {
                if (mAllTrack.contains(it)) {
                    mAllTrack.remove(it)
                }
            }
        }

        override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
            super.onUserPublished(p0, p1)
            afterPublished(p0, p1, true)
        }

        override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
            super.onUserUnpublished(p0, p1)
            p1.forEach {
                if (mAllTrack.contains(it)) {
                    mAllTrack.remove(it)
                }
                //  mUserBindCameraWindowMap.remove(it)
            }
        }

        override fun onUserLeft(p0: String) {
            if (RoomManager.mCurrentRoom?.provideMeId() == p0) {
                mAllTrack.clear()
                mUserUnbindCameraWindowMap.clear()//tod0
                localVideoTrack = null
                localAudioTrack = null
                isAudioEnable = false
                isVideoEnable = false
            } else {
                val toRemove = ArrayList<QNTrack>()
                mAllTrack.forEach {
                    if (it.userID == p0) {
                        toRemove.add(it)
                        //mUserBindCameraWindowMap.remove(it)
                    }
                }
                mAllTrack.removeAll(toRemove)
                mUserUnbindCameraWindowMap.remove(p0)
            }
        }

        override fun onSubscribed(
            p0: String,
            p1: MutableList<QNRemoteAudioTrack>?,
            p2: MutableList<QNRemoteVideoTrack>?
        ) {
            Log.d("mUserUnbindCa", " onSubscribed ${p0}")
            super.onSubscribed(p0, p1, p2)
        }
    }

    protected open fun afterPublished(p0: String, p1: List<QNTrack>, isRemote: Boolean) {
        mAllTrack.addAll(p1)
        p1.forEach { track ->
            when (track.tag) {
                TAG_AUDIO -> {
                    if (isRemote) {
                        mClient.subscribe(track as QNRemoteTrack)
                    }
                }
                TAG_CAMERA -> {
                    Log.d("mUserUnbindCa", "afterPublished  TAG_CAMERA  ${p0}")
                    if (isRemote) {
                        mClient.subscribe(track as QNRemoteTrack)
                    }
                    //提前设置了这个用户的摄像头预览窗口 现在把他绑定
                    mUserUnbindCameraWindowMap[p0]?.let {
                        (track as QNRemoteTrack).tryPlay(it)
                        Log.d(
                            "mUserUnbindCa",
                            "绑定摄像头" + it.width.toString() + "  " + it.height + "  " + it.visibility + "  "
                        )
                    }
                    val v = mUserUnbindCameraWindowMap.remove(p0)
                    //  v?.let {  mUserBindCameraWindowMap.put(track,it) }
                }
            }
        }
    }

    override val mQNRTCEngineEventWrap: QNRTCEngineEventWrap
            by lazy {
                QNRTCEngineEventWrap().apply {
                    addSelfQNRTCEngineEventWrap(this)
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
        localVideoTrack?.play(view)
    }

    /**
     * 设置某人的摄像头预览窗口 可以在任何时候调用
     */
    fun setUserCameraWindowView(uid: String, view: QNTextureView) {
        var isBind = false
        if (uid == com.niucube.comproom.RoomManager.mCurrentRoom?.provideMeId()) {
            setLocalCameraWindowView(view)
            return
        }
        var findTrack = false
        mAllTrack.forEach {
            if (it.tag == TAG_CAMERA && it.userID == uid) {
                findTrack = true
                it.tryPlay(view)
                Log.d("mUserUnbindCa", "setUserCameraWindowView  找打了${uid}")
                return@forEach
            }
        }
        if (!findTrack) {
            Log.d("mUserUnbindCa", "setUserCameraWindowView  没找打${uid}")
            mUserUnbindCameraWindowMap[uid] = view
        }
    }

    protected fun clear() {
        mClientRole = ClientRoleType.CLIENT_ROLE_AUDIENCE
        mAllTrack.clear()
        mUserUnbindCameraWindowMap.clear()
        //   mUserBindCameraWindowMap.clear()
        mIAudienceJoinListeners.clear()
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
            setAudioQuality(microphoneAudioTrackConfig.audioQuality)
            setCommunicationModeOn(microphoneAudioTrackConfig.isCommunicationModeOn)
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
            setCommunicationModeOn(microphoneAudioTrackConfig.communicationModeOn)
        }
    }

    protected var isVideoEnable = false

    protected open fun createVideoTrack() {
        if (localVideoTrack == null) {
            localVideoTrack = createVideoTrack(mQNVideoEncoderConfig)
        }
    }

    protected open fun enableVideo(call: QNPublishResultCallback) {
        if (localVideoTrack == null) {
            localVideoTrack = createVideoTrack(mQNVideoEncoderConfig)
        }
        if (com.niucube.comproom.RoomManager.mCurrentRoom?.isJoined == true) {
            mClient.publish(object : QNPublishResultCallback {
                override fun onPublished() {
                    GlobalScope.launch(Dispatchers.Main) {
                        mQNRTCEngineEventWrap.onLocalPublished(
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
            localAudioTrack = createAudioTrack(mQNMicrophoneAudioTrackConfig)
        }
    }

    protected open fun enableAudio(call: QNPublishResultCallback) {
        if (localAudioTrack == null) {
            localAudioTrack = createAudioTrack(mQNMicrophoneAudioTrackConfig)
        }
        if (com.niucube.comproom.RoomManager.mCurrentRoom?.isJoined == true) {
            mClient.publish(object : QNPublishResultCallback {
                override fun onPublished() {
                    GlobalScope.launch(Dispatchers.Main) {
                        mQNRTCEngineEventWrap.onLocalPublished(
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
            mClient.unpublish(listOf(localVideoTrack))
            mQNRTCEngineEventWrap.onLocalUnpublished(
                RoomManager.mCurrentRoom?.provideMeId() ?: "",
                listOf(localVideoTrack!!)
            )
        }
        isVideoEnable = false
    }

    /**
     * 关闭音频模块
     */
    protected open fun disableAudio() {
        if (com.niucube.comproom.RoomManager.mCurrentRoom?.isJoined == true) {
            mClient.unpublish(listOf(localAudioTrack))
            mQNRTCEngineEventWrap.onLocalUnpublished(
                RoomManager.mCurrentRoom?.provideMeId() ?: "",
                listOf(localAudioTrack!!)
            )
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
        if (uid == com.niucube.comproom.RoomManager.mCurrentRoom?.provideMeId()) {
            return localVideoTrack
        }
        mAllTrack.forEach {
            if (it.tag == TAG_CAMERA && it.userID == uid) {
                return it as QNTrack
            }
        }
        return null
    }

    /**
     * 获取某人的音频轨道
     */
    fun getUserAudioTrackInfo(uid: String): QNTrack? {
        if (uid == com.niucube.comproom.RoomManager.mCurrentRoom?.provideMeId()) {
            return localAudioTrack
        }
        mAllTrack.forEach {
            if (it.tag == TAG_AUDIO && it.userID == uid) {
                return it as QNTrack
            }
        }
        return null
    }

    /**
     * 获得混流器实现
     */
    fun getMixStreamHelper(): MixStreamManager {
        mClient.toString()
        if (mMixStreamHelper == null) {
            mMixStreamHelper = MixStreamHelperImp(
                this,
                mClient,
                localVideoTrack,
                localAudioTrack,
                screenShareManager,
                customTrackShareManager
            )
        }
        return mMixStreamHelper!!
    }

    private var mUserExt: UserExtension? = null

    /**
     * 离开房间
     */
    protected open suspend fun leaveRoom() {
        try {
            if (mClientRole != ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                if (!TextUtils.isEmpty(RoomManager.mCurrentRoom?.provideImGroupId())) {
                    mRtcRoomSignaling.userLeft(mUserExt ?: UserExtension().apply {
                        uid = RoomManager.mCurrentRoom?.provideMeId()
                    })
                }
            }
            RtmManager.rtmClient.leaveChannel(
                com.niucube.comproom.RoomManager.mCurrentRoom?.provideImGroupId() ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        RoomManager.dispatchRoomLeaving()
        clear()
        mClient.leave()
    }


//    protected open fun joinRoom(
//        roomEntity: com.niucube.comproom.RoomEntity,
//        userExt: UserExtension?,
//        callBack: RtcOperationCallback
//    ) {
//
//        GlobalScope.launch(Dispatchers.Main) {
//            try {
//                joinRoom(roomEntity, userExt)
//                callBack.onSuccess()
//            } catch (e: RtcException) {
//                e.printStackTrace()
//                callBack.onFailure(e.code, e.msg)
//            } catch (e: RtmException) {
//                callBack.onFailure(-e.code, e.msg)
//            }
//        }
//    }

    protected open suspend fun joinRoom(
        roomEntity: com.niucube.comproom.RoomEntity,
        userExt: UserExtension?
    ) {
        mUserExt = userExt
        com.niucube.comproom.RoomManager.dispatchRoomEntering(roomEntity)
        //加入im房间
        RtmManager.rtmClient.joinChannel(roomEntity.provideImGroupId())

        if (mClientRole != ClientRoleType.CLIENT_ROLE_BROADCASTER) {
            //如果不是主播角色 发送用户加入通知房间所有人
            mRtcRoomSignaling.userJoin(mUserExt ?: UserExtension().apply {
                uid = roomEntity.provideMeId()
            })
        }
        //拉流角色绑定播放器
        if (mClientRole == ClientRoleType.CLIENT_ROLE_PULLER) {
            mIAudiencePlayerView?.startAudiencePlay(RoomManager.mCurrentRoom!!)
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
        RtmManager.addRtmChannelListener(mRtcRoomSignalingLister)
    }

    /**
     * 销毁房间
     */
    open fun closeRoom() {
        RtmManager.removeRtmChannelListener(mRtcRoomSignalingLister)
        com.niucube.comproom.RoomManager.dispatchCloseRoom()
        mClient?.leave()
        // 反初始化
        QNRTC.deinit()
        clear()
    }

    open class RtcRoomSignalingLister(val rtcRoom: RtcRoom) : RtmMsgListener {
        open fun onNewMsgSignaling(msg: String, peerId: String): Boolean {
            when (msg.optAction()) {
                action_rtc_userJoin -> {
                    val user = JsonUtils.parseObject(msg.optData(), UserExtension::class.java)
                        ?: return true
                    rtcRoom.mIAudienceJoinListeners.forEach {
                        it.onUserJoin(user)
                    }
                    return true
                }
                action_rtc_userLeft -> {
                    val user = JsonUtils.parseObject(msg.optData(), UserExtension::class.java)
                        ?: return true
                    rtcRoom.mIAudienceJoinListeners.forEach {
                        it.onUserLeave(user)
                    }
                    return true
                }
            }
            return false
        }

        /**
         * 收到消息
         */
        override fun onNewMsg(msg: String, peerId: String): Boolean {
            if (peerId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return false
            }
            return onNewMsgSignaling(msg, peerId)
        }
    }
}