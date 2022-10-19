package com.niucube.lazysitmutableroom

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.util.ParameterizedTypeImpl
import com.niucube.comproom.*
import com.niucube.qrtcroom.*
import com.niucube.absroom.*
import com.niucube.absroom.RtcOperationCallback.Companion.error_seat_status
import com.niucube.absroom.seat.MicSeat
import com.niucube.absroom.seat.UserExtension
import com.niucube.qrtcroom.rtc.joinRtc
import com.niucube.rtm.RtmCallBack
import com.niucube.rtm.optAction
import com.niucube.rtm.optData
import com.qiniu.jsonutil.JsonUtils
import com.qiniu.droid.rtc.*
import kotlinx.coroutines.*
import java.lang.reflect.Type
import kotlin.coroutines.*

//固定麦位房间
class LazySitMutableLiverRoom(
    appContext: Context,
    mQNRTCSetting: QNRTCSetting = QNRTCSetting(),
    clientConfig: QNRTCClientConfig = QNRTCClientConfig(QNClientMode.LIVE, QNClientRole.AUDIENCE)
) : BaseMutableRoom<LazySitUserMicSeat>(appContext, mQNRTCSetting, clientConfig) {

    override var mClientRole: com.niucube.comproom.ClientRoleType =
        com.niucube.comproom.ClientRoleType.CLIENT_ROLE_AUDIENCE

    override val mRtcRoomSignalingLister by lazy { FixRtcRoomSignalingLister(this) }
    override val mTrackSeatListener by lazy { FixRoomUserMicSeatListener(this) }

    //添加麦位监听
    fun addUserMicSeatListener(listener: UserMicSeatListener) {
        mTrackSeatListeners.add(listener)
    }

    fun removeUserMicSeatListener(listener: UserMicSeatListener) {
        mTrackSeatListeners.remove(listener)
    }

    // 以观众身份加入房间
    suspend fun joinRoomAsAudience(
        roomEntity: RoomEntity,
        userExt: UserExtension?
    ) {
        mClientRole = ClientRoleType.CLIENT_ROLE_AUDIENCE
        joinRoom(roomEntity, userExt)
    }

    // 以拉流身份加入房间
    suspend fun joinRoomAsPuller(
        roomEntity: RoomEntity,
        userExt: UserExtension?
    ) {
        mClientRole = ClientRoleType.CLIENT_ROLE_PULLER
        joinRoom(roomEntity, userExt)
    }

    public override suspend fun leaveRoom() {
        var isOnMicSeat = false
        var mySeat: LazySitUserMicSeat? = null
        mMicSeats.forEach {
            if (it.isMySeat(mRTCUserStore.joinRoomParams.meId)) {
                isOnMicSeat = true;
                mySeat = it
                return@forEach
            }
        }
        if (isOnMicSeat) {
            mRtcRoomSignaling.sitUp(mySeat!!)
            mTrackSeatListener.onUserSitUp(mySeat!!, false)
        }
        mMicSeats.clear()
        super.leaveRoom()
    }

    override fun closeRoom() {
        super.closeRoom()
    }

    /**
     * 上麦 指定麦位上麦 sdk内部加入rtc房间切换播放为rtc订阅
     * @param cameraParams 如果需要开启摄像头 否则null
     * @param micphoneParams 如果需要开启麦克风 否则null
     */
    suspend fun sitDown(
        userExt: UserExtension?,
        cameraParams: VideoTrackParams?,
        micphoneParams: AudioTrackParams?
    ) {
        Log.d("sitDown","joinRtc")
        if (mClientRole == ClientRoleType.CLIENT_ROLE_PULLER) {
            //如果是从拉流角色上麦需要加入房间 拉流角色是指使用rtmp等协议播放合流
            joinRtc(RoomManager.mCurrentRoom?.provideRoomToken() ?: "", "")
        }
        Log.d("sitDown","setClientRoleSuspend")
        setClientRoleSuspend(ClientRoleType.CLIENT_ROLE_BROADCASTER)
        //设置角色为主播
        val seatTemp = LazySitUserMicSeat().apply {
            isOwnerOpenAudio = micphoneParams != null
            isOwnerOpenVideo = cameraParams != null
        }
        seatTemp.uid = RoomManager.mCurrentRoom?.provideMeId() ?: ""
        seatTemp.userExtension = userExt
        //发送上麦信令
        mRtcRoomSignaling.sitDown(seatTemp)
        Log.d("sitDown","setCameraVideoTrackParams")
        //创建视频轨道
        cameraParams?.let {
            setCameraVideoTrackParams(it)
            createVideoTrack()
        }
        //创建音频轨道
        micphoneParams?.let {
            setMicrophoneAudioParams(it)
            createVideoTrack()
        }
        Log.d("sitDown","suspendEnableVideo")
        //发布流
        cameraParams?.let {
            suspendEnableVideo()
        }
        Log.d("sitDown","suspendEnableAudio")
        //发布流
        micphoneParams?.let {
            suspendEnableAudio()
        }
        mMicSeats.add(seatTemp)
        //回调本地自己上麦
        mTrackSeatListener.onUserSitDown(seatTemp)
    }

    /**
     * 下麦 sdk内部退出rtc房间 切换订阅播放到拉流播放
     * @param seat
     */
    suspend fun sitUpAsAudience() {
        val seat = getUserSeat(RoomManager.mCurrentRoom?.provideMeId() ?: "")
            ?: throw RtcOperationException(error_seat_status, "user is not on seat")
        //切换角色
        setClientRoleSuspend(ClientRoleType.CLIENT_ROLE_AUDIENCE)
        //发送下麦信令
        mRtcRoomSignaling.sitUp(seat)
        super.onlyDisableAudio()
        super.onlyDisableVideo()
        //  mEngine.leave()
        mMicSeats.remove(seat)
        mTrackSeatListener.onUserSitUp(seat, false)
    }

    //下麦后拉流
    suspend fun sitUpAsPuller() {
        val seat = getUserSeat(RoomManager.mCurrentRoom?.provideMeId() ?: "")
            ?: throw RtcOperationException(error_seat_status, "user is not on seat")
        mRtcRoomSignaling.sitUp(seat)
        setClientRoleSuspend(ClientRoleType.CLIENT_ROLE_PULLER)
        super.onlyDisableAudio()
        super.onlyDisableVideo()
        leaveRtc()
        mMicSeats.remove(seat)
        mTrackSeatListener.onUserSitUp(seat, false)
        seat.clear()
    }

    public override fun muteLocalVideo(muted: Boolean) {
        super.muteLocalVideo(muted)
    }

    public override fun muteLocalAudio(muted: Boolean) {
        super.muteLocalAudio(muted)
    }

    public override fun muteRemoteAudio(uid: String, muted: Boolean) {
        super.muteRemoteAudio(uid, muted)
    }

    public override fun muteRemoteVideo(uid: String, muted: Boolean) {
        super.muteRemoteVideo(uid, muted)
    }

    public override fun muteAllRemoteVideo(muted: Boolean) {
        super.muteAllRemoteVideo(muted)
    }

    public override fun muteAllRemoteAudio(muted: Boolean) {
        super.muteAllRemoteAudio(muted)
    }

    //从麦位上踢出
    fun kickOutFromMicSeat(uid: String, msg: String, callBack: RtcOperationCallback) {
        val seat = getUserSeat(uid)
        if (seat == null) {
            callBack.onFailure(error_seat_status, "seatId error")
            return
        }
        if (TextUtils.isEmpty(seat.uid)) {
            callBack.onFailure(error_seat_status, "seat is  empty")
            return
        }
        mRtcRoomSignaling.kickOutFromMicSeat(
            UserMicSeatMsg(seat, msg),
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack.onSuccess()
                    //mEngine.k
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack.onFailure(-code, msg)
                }
            })
    }

    //从房间踢出
    fun kickOutFromRoom(userId: String, msg: String, callBack: RtcOperationCallback) {
        mRtcRoomSignaling.kickOutFromRoom(userId, msg, object : RtmCallBack {
            override fun onSuccess() {
                callBack.onSuccess()
                // mEngine.kickOutUser(userId)
            }

            override fun onFailure(code: Int, msg: String) {
                callBack.onFailure(-code, msg)
            }
        })
    }

    //禁止开麦克风
    fun forbiddenMicSeatAudio(
        uid: String,
        isForbidden: Boolean,
        msg: String,
        callBack: RtcOperationCallback
    ) {
        val seat = getUserSeat(uid)
        if (seat == null) {
            callBack.onFailure(error_seat_status, "seatId error")
            return
        }
        if (TextUtils.isEmpty(seat.uid)) {
            callBack.onFailure(error_seat_status, "seat is  empty")
            return
        }
        mRtcRoomSignaling.forbiddenMicSeatAudio(
            ForbiddenMicSeatMsg(
                uid,
                isForbidden,
                msg
            ),
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack.onSuccess()
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack.onFailure(-code, msg)
                }
            })
    }

    //禁止开摄像头
    fun forbiddenMicSeatVideo(
        uid: String,
        isForbidden: Boolean,
        msg: String,
        callBack: RtcOperationCallback
    ) {
        val seat = getUserSeat(uid)
        if (seat == null) {
            callBack.onFailure(error_seat_status, "seatId error")
            return
        }

        if (TextUtils.isEmpty(seat.uid)) {
            callBack.onFailure(error_seat_status, "seat is  empty")
            return
        }

        mRtcRoomSignaling.forbiddenMicSeatVideo(
            ForbiddenMicSeatMsg(
                uid,
                isForbidden,
                msg
            ),
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack.onSuccess()
                }
                override fun onFailure(code: Int, msg: String) {
                    callBack.onFailure(-code, msg)
                }
            })
    }

    /**
     * 自定义麦位操作
     * @param seat
     * @param action
     */
    fun sendCustomSeatAction(
        uid: String,
        key: String,
        values: String,
        callBack: RtcOperationCallback
    ) {
        val seat = getUserSeat(uid)
        if (seat == null) {
            callBack.onFailure(error_seat_status, "seatId error")
            return
        }
        mRtcRoomSignaling.sendCustomSeatAction(
            CustomSeatAction(
                uid,
                key,
                values
            ),
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack.onSuccess()
                    mTrackSeatListener.onCustomSeatAction(seat, key, values)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack.onFailure(-code, msg)
                }
            })
    }

    class FixRoomUserMicSeatListener(val lazyRoom: LazySitMutableLiverRoom) :
        InnerBaseMicSeatListener<LazySitUserMicSeat>(lazyRoom), UserMicSeatListener {

        override fun onVideoForbiddenStatusChanged(seat: LazySitUserMicSeat, msg: String) {
            lazyRoom.mTrackSeatListeners.forEach {
                (it as UserMicSeatListener).onVideoForbiddenStatusChanged(seat, msg)
            }
        }

        override fun onAudioForbiddenStatusChanged(seat: LazySitUserMicSeat, msg: String) {
            lazyRoom.mTrackSeatListeners.forEach {
                (it as UserMicSeatListener).onAudioForbiddenStatusChanged(seat, msg)
            }
        }

        override fun onKickOutFromMicSeat(seat: LazySitUserMicSeat, msg: String) {
            lazyRoom.mTrackSeatListeners.forEach {
                (it as UserMicSeatListener).onKickOutFromMicSeat(seat, msg)
            }
        }

        override fun onKickOutFromRoom(userId: String, msg: String) {
            lazyRoom.mTrackSeatListeners.forEach {
                (it as UserMicSeatListener).onKickOutFromRoom(userId, msg)
            }
        }

        override fun onCustomSeatAction(seat: MicSeat, key: String, values: String) {
            lazyRoom.mTrackSeatListeners.forEach {
                (it as UserMicSeatListener).onCustomSeatAction(seat, key, values)
            }
        }
    }

    class FixRtcRoomSignalingLister(val lazyRoom: LazySitMutableLiverRoom) :
        MutableRtcRoomSignalingLister<LazySitUserMicSeat>(
            LazySitUserMicSeat::class.java,
            lazyRoom
        ) {

        override fun onNewMsgSignaling(msg: String, peerId: String): Boolean {
            when (msg.optAction()) {
                action_rtc_kickOutFromMicSeat -> {
                    val type = ParameterizedTypeImpl(
                        arrayOf<Type>(LazySitUserMicSeat::class.java),
                        UserMicSeatMsg::class.java,
                        UserMicSeatMsg::class.java
                    )
                    val seat =
                        JsonUtils.parseObject<UserMicSeatMsg<LazySitUserMicSeat>>(
                            msg.optData(),
                            type
                        )
                            ?: return true
//                    onKickUserMic(seat.seat?.uid ?: "") {
//                        lazyRoom.mTrackSeatListener.onKickOutFromMicSeat(it, msg)
//                    }
                    lazyRoom.mTrackSeatListener.onKickOutFromMicSeat(seat.seat!!, msg)
                    return true
                }
                action_rtc_kickOutFromRoom -> {
                    val uidAndMsg =
                        JsonUtils.parseObject(msg.optData(), UidAndMsg::class.java)
                            ?: return true
                    // onKickUser(uidAndMsg.uid) {}
                    lazyRoom.mTrackSeatListener.onKickOutFromRoom(uidAndMsg.uid, msg)
                    return true
                }
                action_rtc_forbiddenAudio -> {
                    val forbiddenMicSeatMsg = JsonUtils.parseObject(
                        msg.optData(),
                        ForbiddenMicSeatMsg::class.java
                    )
                        ?: return true
                    lazyRoom.getUserSeat(forbiddenMicSeatMsg.uid)?.let {
                        it.isForbiddenAudioByManager = forbiddenMicSeatMsg.isForbidden
//                        if (it.isMySeat() && it.isForbiddenAudioByManager
//                            && it.isOwnerOpenAudio
//                        ) {
//                            lazyRoom.localAudioTrack?.isMuted=true
//                        }
                        lazyRoom.mTrackSeatListener.onAudioForbiddenStatusChanged(
                            it,
                            forbiddenMicSeatMsg.msg
                        )
                    }
                    return true
                }
                action_rtc_forbiddenVideo -> {
                    val forbiddenMicSeatMsg = JsonUtils.parseObject(
                        msg.optData(),
                        ForbiddenMicSeatMsg::class.java
                    )
                        ?: return true
                    lazyRoom.getUserSeat(forbiddenMicSeatMsg.uid)?.let {
                        it.isForbiddenVideoByManager = forbiddenMicSeatMsg.isForbidden
//                        if (it.isMySeat() && it.isForbiddenVideoByManager
//                            && it.isOwnerOpenVideo
//                        ) {
//                            lazyRoom.localVideoTrack?.isMuted = true
//                        }
                        lazyRoom.mTrackSeatListener.onVideoForbiddenStatusChanged(
                            it,
                            forbiddenMicSeatMsg.msg
                        )
                    }
                    return true
                }
                action_rtc_customSeatAction -> {
                    val customSeatAction = JsonUtils.parseObject(
                        msg.optData(),
                        CustomSeatAction::class.java
                    )
                        ?: return true
                    lazyRoom.getUserSeat(customSeatAction.uid)?.let {
                        lazyRoom.mTrackSeatListener.onCustomSeatAction(
                            it,
                            customSeatAction.key,
                            customSeatAction.values
                        )
                    }
                    return true
                }
                action_rtc_sitDown -> {
                    val seat = JsonUtils.parseObjectType<LazySitUserMicSeat>(
                        msg.optData(),
                        LazySitUserMicSeat::class.java
                    )
                        ?: return true
                    if (lazyRoom.getUserSeat(seat.uid) == null) {
                        lazyRoom.mMicSeats.add(seat)
                        lazyRoom.mTrackSeatListener.onUserSitDown(seat)
                    }
                    return true
                }
            }
            return super.onNewMsgSignaling(msg, peerId)
        }
    }
}