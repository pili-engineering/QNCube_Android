package com.niucube.singleliverroom

import android.content.Context
import com.niucube.absroom.*
import com.niucube.comproom.ClientRoleType
import com.niucube.rtcroom.*
import com.niucube.absroom.seat.UserExtension
import com.niucube.absroom.seat.UserMicSeat
import com.niucube.comproom.provideMeId
import com.niucube.rtm.RtmCallBack
import com.niucube.rtm.RtmException
import com.niucube.rtm.optAction
import com.niucube.rtm.optData
import com.qiniu.droid.rtc.QNRTCSetting
import com.qiniu.jsonutil.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class RtcLivingRoom(
    appContext: Context,
    mQNRTCSetting: QNRTCSetting = QNRTCSetting()
) : RtcRoom(appContext, mQNRTCSetting) {
    //
    private val livingListeners = ArrayList<LivingListener>()
    var mLiverMicSeat: UserMicSeat = UserMicSeat()
        private set

    override val mRtcRoomSignalingLister by lazy {
        LivingRtcRoomSignalingLister(this)
    }

    //设置角色 CLIENT_ROLE_BROADCASTER / CLIENT_ROLE_AUDIENCE
    //默认用户
    //不支持角色切换
    fun setClientRole(role: ClientRoleType, callBack: RtcOperationCallback) {
        if (mClientRole == ClientRoleType.CLIENT_ROLE_PULLER) {
            mClientRole = role
        }
    }

    //用户角色初始化主播麦位
    fun userClientTypeSyncMicSeats(liverMicSeat: UserMicSeat) {
        mLiverMicSeat = liverMicSeat
    }


    public override fun enableVideo(params: VideoTrackParams?) {
        super.enableVideo(params ?: VideoTrackParams())
    }

    public override fun enableAudio(params: AudioTrackParams?) {
        super.enableAudio(params ?: AudioTrackParams())
    }

    public override fun disableVideo() {
        super.disableVideo()
    }

    public override fun disableAudio() {
        super.disableAudio()
    }

    //加入房间 参数：抽象房间实体 ，用户扩展字段（可为空）->可以标记扩展角色，加入房间的资料，扩展信息
    //主播身份加入成功即是上麦
    override fun joinRoom(
        roomEntity: com.niucube.comproom.RoomEntity,
        userExt: UserExtension?,
        callBack: RtcOperationCallback
    ) {
        val seat = UserMicSeat().apply {
            uid = roomEntity.provideMeId() ?: ""
            userExtension = userExt
        }
        super.joinRoom(roomEntity, userExt, object : RtcOperationCallback {
            override fun onSuccess() {
                if (mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                    mLiverMicSeat = seat
                    mLivingListener.onLiverSitDown(seat)
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            mRtcRoomSignaling.sitDown(seat)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    callBack.onSuccess()
                }
            }

            override fun onFailure(errorCode: Int, msg: String) {
                callBack.onFailure(errorCode, msg)
            }
        })
    }

    override fun leaveRoom(callBack: RtcOperationCallback) {
        val superCall = {
            super.leaveRoom(callBack)
        }
        val seat = mLiverMicSeat
        if (mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    mRtcRoomSignaling.sitUp(seat)
                    superCall.invoke()
                } catch (e: RtmException) {
                    e.printStackTrace()
                    callBack.onFailure(-e.code, e.msg)
                }
            }
        } else {
            superCall.invoke()
        }
    }

    override fun closeRoom() {
        super.closeRoom()
    }

    fun addLivingListener(livingListener: LivingListener) {
        livingListeners.add(livingListener)
    }

    fun removeLivingListener(livingListener: LivingListener) {
        livingListeners.remove(livingListener)
    }

    private var mLinkMicManager :LinkMicManager?=null
    //获得连麦管理
    fun getLinkMicManager(): LinkMicManager {
        if(mLinkMicManager==null){
            mLinkMicManager = LinkMicManagerImp(this)
        }
        return mLinkMicManager!!
    }

    fun getPkManger(): PkManger {
        return PkManger()
    }

    fun kickOutFromRoom(userId: String, msg: String, callBack: RtcOperationCallback) {
        mRtcRoomSignaling.kickOutFromRoom(userId, msg, object : RtmCallBack {
            override fun onSuccess() {
                callBack.onSuccess()
            }

            override fun onFailure(code: Int, msg: String) {
                callBack.onFailure(-code, msg)
            }
        })
    }

    private val mLivingListener = object : LivingListener {
        override fun onLiverSitDown(seat: UserMicSeat) {
            livingListeners.forEach {
                it.onLiverSitDown(seat)
            }
        }

        //
        override fun onLiverSitUp(seat: UserMicSeat, isOffLine: Boolean) {
            livingListeners.forEach {
                it.onLiverSitUp(seat, isOffLine)
            }
        }

        override fun onLiverAudioStatusChange(seat: UserMicSeat) {
            livingListeners.forEach {
                it.onLiverAudioStatusChange(seat)
            }
        }

        override fun onLiverCameraStatusChange(seat: UserMicSeat) {
            livingListeners.forEach {
                onLiverCameraStatusChange(seat)
            }
        }

        override fun onKickOutFromRoom(userId: String,msg:String) {
            livingListeners.forEach {
                it.onKickOutFromRoom(userId,msg)
            }
        }
    }

    class LivingRtcRoomSignalingLister(val room: RtcLivingRoom) :
        RtcRoomSignalingLister(room) {
        override fun onNewMsgSignaling(msg: String, peerId: String): Boolean {
            when (msg.optAction()) {
                action_rtc_microphoneStatus -> {
                    if (room.mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
                        val seat = JsonUtils.parseObject(msg.optData(), UserMicSeat::class.java)
                            ?: return true
                        room.mLiverMicSeat.let {
                            it.isOwnerOpenAudio = seat.isOwnerOpenAudio
                            room.mLivingListener.onLiverAudioStatusChange(it)
                        }
                    }
                    return true
                }

                action_rtc_cameraStatus -> {
                    if (room.mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
                        val seat = JsonUtils.parseObject(msg.optData(), UserMicSeat::class.java)
                            ?: return true
                        room.mLiverMicSeat.let {
                            it.isOwnerOpenVideo = seat.isOwnerOpenVideo
                            room.mLivingListener.onLiverCameraStatusChange(it)
                        }
                    }
                    return true
                }

                action_rtc_sitDown -> {
                    if (room.mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
                        val seat = JsonUtils.parseObject(msg.optData(), UserMicSeat::class.java)
                            ?: return true
                        room.mLiverMicSeat.userExtension = seat.userExtension
                        room.mLivingListener.onLiverSitDown(seat)
                    }
                    return true
                }
                action_rtc_sitUp -> {
                    val seat = JsonUtils.parseObject(msg.optData(), UserMicSeat::class.java)
                        ?: return true
                    room.mLiverMicSeat.let {
                        room.mLivingListener.onLiverSitUp(it, false)
                    }
                    return true
                }
                action_rtc_kickOutFromRoom -> {
                    val uidAndMsg =
                        JsonUtils.parseObject(msg.optData(), RtcRoomSignaling.UidAndMsg::class.java)
                            ?: return true
                    room.mLivingListener.onKickOutFromRoom(uidAndMsg.uid, uidAndMsg.msg)
                    return true
                }
            }
            return super.onNewMsgSignaling(msg, peerId)
        }
    }
}