package com.niucube.singleliverroom

import android.util.Log
import com.niucube.absroom.*
import com.niucube.absroom.RtcOperationCallback.Companion.error_room_not_join
import com.niucube.absroom.RtcOperationCallback.Companion.error_room_role_no_permission
import com.niucube.absroom.RtcOperationCallback.Companion.error_seat_status
import com.niucube.absroom.seat.UserExtension
import com.niucube.absroom.seat.UserMicSeat
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomManager
import com.niucube.comproom.provideMeId
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.niucube.rtcroom.RtcException
import com.niucube.rtcroom.joinRtc
import com.niucube.rtm.RtmException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LinkMicManagerImp(val rtmRoom: RtcLivingRoom) : LinkMicManager {
    override fun userClientTypeSyncMicSeats(micSeats: List<UserMicSeat>) {
        TODO("Not yet implemented")
    }

    override fun setLinkMicSeatListener(lister: LinkMicSeatListener) {
        TODO("Not yet implemented")
    }

    /**
     * 上麦
     *
     */
    override fun sitDown(
        userExt: UserExtension,
        videoTrackParams: VideoTrackParams?,
        audioTrackParams: AudioTrackParams?,
        callBack: RtcOperationCallback
    ) {
        TODO("Not yet implemented")
    }

    /**
     * 下麦
     * @param seat
     */
    override fun sitUp(callBack: RtcOperationCallback) {
        TODO("Not yet implemented")
    }

    override fun kickOutFromMicSeat(
        seat: UserMicSeat,
        msg: String,
        callBack: RtcOperationCallback
    ) {
        TODO("Not yet implemented")
    }
    //用户角色初次进入房间 需要业务服务器同步一下当前房间正在连麦的人
//
//
//    private val mMicSeats = ArrayList<UserMicSeat>()
//    private var mLinkMicSeatListener: LinkMicSeatListener? = null
//    private var mRtcRoomSignaling = RtcRoomSignaling()
//
//    private val mRtcEvent  = object : SimpleQNRTCListener {
//
//        override fun onError(p0: Int, p1: String) {
//            super.onError(p0, p1)
//
//        }
//    }
//    override fun userClientTypeSyncMicSeats(micSeats: List<UserMicSeat>) {
//        mMicSeats.clear()
//        mMicSeats.addAll(micSeats)
//    }
//
//    override fun setLinkMicSeatListener(lister: LinkMicSeatListener) {
//        mLinkMicSeatListener = lister
//    }
//
//    /**
//     * 上麦
//     *
//     */
//    override fun sitDown(
//        userExt: UserExtension,
//        videoTrackParams: VideoTrackParams?,
//        audioTrackParams: AudioTrackParams?,
//        callBack: RtcOperationCallback
//    ) {
//        if (RoomManager.mCurrentRoom == null) {
//            callBack.onFailure(error_room_not_join, "error_room_not_join")
//        }
//        if (rtmRoom.mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
//            GlobalScope.launch(Dispatchers.Main) {
//                try {
//                    rtmRoom.joinRtc(RoomManager.mCurrentRoom?.provideRoomToken() ?: "", "")
//                    val userMicSeat = UserMicSeat()
//                    userMicSeat.isMySeat() = true
//                    userMicSeat.uid = RoomManager.mCurrentRoom?.provideMeId()
//                    userMicSeat.userExtension = userExt
//                    if (videoTrackParams != null) {
//                        userMicSeat.isOwnerOpenVideo = true
//                        rtmRoom.enableVideo(videoTrackParams)
//                    }
//                    if (audioTrackParams != null) {
//                        userMicSeat.isOwnerOpenAudio = true
//                        rtmRoom.enableAudio(audioTrackParams)
//                    }
//                    mRtcRoomSignaling.sitDownLinkerMic(userMicSeat)
//                    mLinkMicSeatListener?.onMicLinkerSitDown(userMicSeat)
//                    mMicSeats.add(userMicSeat)
//                    callBack.onSuccess()
//                } catch (e: RtcException) {
//                    e.printStackTrace()
//                    rtmRoom.mEngine.leaveRoom()
//                    callBack.onFailure(e.code,e.msg)
//                } catch (e: RtmException) {
//                    rtmRoom.mEngine.leaveRoom()
//                    callBack.onFailure(-e.code,e.msg)
//                }
//            }
//        } else {
//            callBack.onFailure(
//                error_room_role_no_permission,
//                "BROADCASTER no permission to link mic"
//            )
//        }
//    }
//
//    /**
//     * 下麦
//     * @param seat
//     */
//    override fun sitUp(callBack: RtcOperationCallback) {
//        var micSeat: UserMicSeat? = null
//        mMicSeats.forEach {
//            if (it.isMySeat()) {
//                micSeat = it
//            }
//        }
//        if (micSeat == null) {
//            callBack.onFailure(error_seat_status, "我不在麦位上")
//            return
//        }
//        GlobalScope.launch(Dispatchers.Main) {
//            try {
//                mRtcRoomSignaling.sitUpLinkerMic(micSeat!!)
//                rtmRoom.mEngine.leaveRoom()
//                mMicSeats.remove(micSeat!!)
//                mLinkMicSeatListener?.onMicLinkerSitUp(micSeat!!,false)
//                callBack.onSuccess()
//            } catch (e: RtcException) {
//                e.printStackTrace()
//                callBack.onFailure(e.code,e.msg)
//            } catch (e: RtmException) {
//                callBack.onFailure(-e.code,e.msg)
//            }
//        }
//    }
//
//    //踢麦
//    override fun kickOutFromMicSeat(
//        seat: UserMicSeat,
//        msg: String,
//        callBack: RtcOperationCallback
//    ) {
//
//    }


}