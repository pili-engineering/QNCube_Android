package com.niucube.basemutableroom

import android.content.Context
import com.niucube.absroom.*
import com.niucube.comproom.*
import com.niucube.rtcroom.customtrack.CustomTrackShareManager
import com.niucube.rtcroom.screencapture.ScreenShareManager
import com.niucube.rtm.optAction
import com.niucube.rtm.optData
import com.qiniu.compim.*
import com.qiniu.droid.rtc.*
import com.qiniu.jsonutil.JsonUtils
import com.niucube.qnrtcsdk.QNRTCEngineEventWrap
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.niucube.rtcroom.RtcRoom
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 基础rtc多人房间
 *
 * @param mQNRTCSetting 开播设置
 */
open class BaseMutableRoom<T : BaseMutableMicSeat>(
    open val app: Context,
    open val rtcSetting: QNRTCSetting = QNRTCSetting(),
    open val clientConfig: QNRTCClientConfig = QNRTCClientConfig(
        QNClientMode.LIVE,
        QNClientRole.BROADCASTER
    )
) : RtcRoom(app, rtcSetting, clientConfig) {
    private val TAG = "RtcRoom"

    override val mQNRTCEngineEventWrap by lazy {
        QNRTCEngineEventWrap().apply {
            addSelfQNRTCEngineEventWrap(this)
        }
    }

    override fun addSelfQNRTCEngineEventWrap(eventWrap: QNRTCEngineEventWrap) {
        super.addSelfQNRTCEngineEventWrap(eventWrap)
        eventWrap.addExtraQNRTCEngineEventListener(MutableSimpleQNRTCListener(this@BaseMutableRoom))
    }

    fun getUserSeat(uid: String): T? {
        mMicSeats.forEach {
            if (uid == it.uid) {
                return it
            }
        }
        return null
    }

    //所有麦位
    val mMicSeats = ArrayList<T>()

    /**
     * 座位监听
     */
    protected var mTrackSeatListeners = ArrayList<BaseMicSeatListener<T>>()

    override val mRtcRoomSignalingLister by lazy {
        val types = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
        MutableRtcRoomSignalingLister(types[0], this)
    }

    protected open val mTrackSeatListener by lazy {
        InnerBaseMicSeatListener(this)
    }

//    override var mClientRole: com.niucube.comproom.ClientRoleType =
//        com.niucube.comproom.ClientRoleType.CLIENT_ROLE_NULL

    //用户角色同步麦位 用户角色使用信令监听可能出错，当前版本需业务方自己往业务服务器同步，后期sdk内部搞定
    fun userClientTypeSyncMicSeats(micSeats: List<T>) {
        mMicSeats.clear()
        mMicSeats.addAll(micSeats)
        mTrackSeatListeners.forEach {
            it.onSyncMicSeats(mMicSeats)
        }
    }

    //获的屏幕共享实现
    fun getScreenShareManager(): ScreenShareManager {
        return screenShareManager
    }

    fun getCustomTrackManager(): CustomTrackShareManager {
        return customTrackShareManager
    }

    protected override fun disableVideo() {
        super.disableVideo()
        getUserSeat(RoomManager.mCurrentRoom?.provideMeId() ?: "")?.let {
            it.isOwnerOpenVideo = false
            mTrackSeatListener.onCameraStatusChanged(it)
        }
    }

    protected override fun disableAudio() {
        super.disableAudio()
        getUserSeat(RoomManager.mCurrentRoom?.provideMeId() ?: "")?.let {
            it.isOwnerOpenAudio = false
            mTrackSeatListener.onMicAudioStatusChanged(it)
        }
    }

    protected fun onlyDisableAudio() {
        super.disableAudio()
    }

    protected fun onlyDisableVideo() {
        super.disableVideo()
    }

    override fun muteLocalVideo(muted: Boolean) {
        super.muteLocalVideo(muted)
        getUserSeat(RoomManager.mCurrentRoom?.provideMeId() ?: "")?.let {
            it.isOwnerOpenVideo = !muted
            mTrackSeatListener.onCameraStatusChanged(it)
            mRtcRoomSignaling.sendCameraStatus(it)
        }
    }

    override fun muteLocalAudio(muted: Boolean) {
        super.muteLocalAudio(muted)
        getUserSeat(RoomManager.mCurrentRoom?.provideMeId() ?: "")?.let {
            it.isOwnerOpenAudio = !muted
            mTrackSeatListener.onMicAudioStatusChanged(it)
            mRtcRoomSignaling.sendMicrophoneStatus(it)
        }
    }

    protected open fun muteRemoteAudio(uid: String, muted: Boolean) {
        getUserSeat(uid)?.let {
            it.isMuteAudioByMe = muted
            mTrackSeatListener.onMicAudioStatusChanged(it)
        }
    }

    protected open fun muteRemoteVideo(uid: String, muted: Boolean) {
        getUserSeat(uid)?.let {
            it.isMuteVideoByMe = muted
            mTrackSeatListener.onCameraStatusChanged(it)
        }
    }

    protected open fun muteAllRemoteVideo(muted: Boolean) {
        mMicSeats.forEach {
            if (!it.isMySeat()) {
                it.isMuteVideoByMe = muted
                mTrackSeatListener.onCameraStatusChanged(it)
            }
        }
    }

    protected open fun muteAllRemoteAudio(muted: Boolean) {
        mMicSeats.forEach {
            if (!it.isMySeat()) {
                it.isMuteAudioByMe = muted
                mTrackSeatListener.onMicAudioStatusChanged(it)
            }
        }
    }

    override fun afterPublished(p0: String, p1: List<QNTrack>, isRemote: Boolean) {
        super.afterPublished(p0, p1, isRemote)
        p1.forEach {
            if (it is QNRemoteAudioTrack) {
                it.setTrackInfoChangedListener(object : QNTrackInfoChangedListener {
                    override fun onMuteStateChanged(isMuted: Boolean) {
                        //    super.onMuteStateChanged(isMuted)
                        getUserSeat(p0)?.let {
                            it.isOwnerOpenAudio = !isMuted
                            mTrackSeatListener.onMicAudioStatusChanged(it)
                        }
                    }
                })
            }
            if (it is QNRemoteVideoTrack) {
                it.setTrackInfoChangedListener(object : QNTrackInfoChangedListener {
                    override fun onMuteStateChanged(isMuted: Boolean) {
                        //     super.onMuteStateChanged(isMuted)
                        getUserSeat(p0)?.let {
                            it.isOwnerOpenVideo = !isMuted
                            mTrackSeatListener.onCameraStatusChanged(it)
                        }
                    }
                })
            }

            when (it.tag) {
                TAG_AUDIO -> {
                    getUserSeat(p0)?.let {
                        it.isOwnerOpenAudio = true
                        mTrackSeatListener.onMicAudioStatusChanged(it)
                        if (!isRemote) {
                            mRtcRoomSignaling.sendMicrophoneStatus(it)
                        }
                    }
                }
                TAG_CAMERA -> {
                    getUserSeat(p0)?.let {
                        it.isOwnerOpenVideo = true
                        mTrackSeatListener.onCameraStatusChanged(it)
                        if (!isRemote) {
                            mRtcRoomSignaling.sendCameraStatus(it)
                        }
                    }
                }
            }
        }
    }

    open class MutableSimpleQNRTCListener<T : BaseMutableMicSeat>(val room: BaseMutableRoom<T>) :
        SimpleQNRTCListener {

        override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
            super.onUserUnpublished(p0, p1)
            p1.forEach {
                when (it.tag) {
                    TAG_CAMERA -> {
                        room.getUserSeat(p0)?.let {
                            it.isOwnerOpenVideo = false
                            room.mTrackSeatListener.onCameraStatusChanged(it)
                        }
                    }
                    TAG_AUDIO -> {
                        room.getUserSeat(p0)?.let {
                            it.isOwnerOpenAudio = false
                            room.mTrackSeatListener.onMicAudioStatusChanged(it)
                        }
                    }
                }
            }
        }

        override fun onUserLeft(p0: String) {
            super.onUserLeft(p0)
            room.getUserSeat(p0)?.let {
                room.mMicSeats.remove(it)
                room.mTrackSeatListener.onUserSitUp(it, true)
            }
        }
    }

    open class InnerBaseMicSeatListener<T : BaseMutableMicSeat>(val room: BaseMutableRoom<T>) :
        BaseMicSeatListener<T> {
        override fun onUserSitDown(micSeat: T) {
            room.mTrackSeatListeners.forEach {
                it.onUserSitDown(micSeat)
            }
        }

        override fun onUserSitUp(micSeat: T, isOffLine: Boolean) {
            room.mTrackSeatListeners.forEach {
                it.onUserSitUp(micSeat, isOffLine)
            }
        }

        override fun onCameraStatusChanged(micSeat: T) {
            room.mTrackSeatListeners.forEach {
                it.onCameraStatusChanged(micSeat)
            }
        }

        override fun onMicAudioStatusChanged(micSeat: T) {
            room.mTrackSeatListeners.forEach {
                it.onMicAudioStatusChanged(micSeat)
            }
        }
    }

    open class MutableRtcRoomSignalingLister<T : BaseMutableMicSeat>(
        val type: Type,
        val room: BaseMutableRoom<T>
    ) :
        RtcRoomSignalingLister(room) {

        override fun onNewMsgSignaling(msg: String, peerId: String): Boolean {
            val tClass = type
            when (msg.optAction()) {
                action_rtc_microphoneStatus -> {
                    if (room.mClientRole == ClientRoleType.CLIENT_ROLE_PULLER) {
                        val seat = JsonUtils.parseObjectType<T>(msg.optData(), tClass)
                            ?: return true
                        room.getUserSeat(seat.uid)?.let {
                            it.isOwnerOpenAudio = seat.isOwnerOpenAudio
                            room.mTrackSeatListener.onMicAudioStatusChanged(it)
                        }
                    }
                    return true
                }

                action_rtc_cameraStatus -> {
                    if (room.mClientRole == ClientRoleType.CLIENT_ROLE_PULLER) {
                        val seat = JsonUtils.parseObjectType<T>(msg.optData(), tClass)
                            ?: return true
                        room.getUserSeat(seat.uid)?.let {
                            it.isOwnerOpenVideo = seat.isOwnerOpenVideo
                            room.mTrackSeatListener.onCameraStatusChanged(it)
                        }
                    }
                    return true
                }

                action_rtc_sitDown -> {
                    if (room.mClientRole == ClientRoleType.CLIENT_ROLE_PULLER) {
                        val seat = JsonUtils.parseObjectType<T>(msg.optData(), tClass)
                            ?: return true
                        room.mMicSeats.add(seat)
                        room.mTrackSeatListener.onUserSitDown(seat)
                    }
                    return true
                }
                action_rtc_sitUp -> {
                    val seat = JsonUtils.parseObjectType<T>(msg.optData(), tClass)
                        ?: return true
                    room.getUserSeat(seat.uid)?.let {
                        room.mMicSeats.remove(it)
                        room.mTrackSeatListener.onUserSitUp(it, false)
                    }
                    return true
                }
            }
            return super.onNewMsgSignaling(msg, peerId)
        }
    }
}