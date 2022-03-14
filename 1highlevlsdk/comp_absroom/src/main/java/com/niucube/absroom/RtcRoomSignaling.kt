package com.niucube.absroom

import com.niucube.comproom.RoomManager
import com.niucube.absroom.seat.CustomMicSeat
import com.niucube.absroom.seat.ScreenMicSeat
import com.niucube.absroom.seat.UserExtension
import com.niucube.absroom.seat.UserMicSeat
import com.niucube.rtm.RtmCallBack
import com.niucube.rtm.RtmException
import com.niucube.rtm.RtmManager
import com.niucube.rtm.msg.RtmTextMsg
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class RtcRoomSignaling() {

    var isNeedSend = true

    fun onCustomMicSeatAdd(seat: CustomMicSeat) {
        if (!isNeedSend) {
            return
        }
        if (seat.isMySeat()) {
            val textMsg = RtmTextMsg<CustomMicSeat>(
                action_rtc_pubCustomTrack,
                seat
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                false,
                null
            )
        }
    }

    fun onCustomMicSeatRemove(seat: CustomMicSeat) {
        if (!isNeedSend) {
            return
        }
        if (seat.isMySeat()) {
            val textMsg = RtmTextMsg<CustomMicSeat>(
                action_rtc_unPubCustomTrack,
                seat
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                false,
                null
            )
        }
    }


    fun onScreenMicSeatAdd(seat: ScreenMicSeat) {
        if (!isNeedSend) {
            return
        }
        if (seat.isMySeat()) {
            val textMsg = RtmTextMsg<ScreenMicSeat>(
                action_rtc_pubScreen,
                seat
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                false,
                null
            )
        }
    }

    fun onScreenMicSeatRemove(seat: ScreenMicSeat) {
        if (!isNeedSend) {
            return
        }
        if (seat.isMySeat()) {
            val textMsg = RtmTextMsg<ScreenMicSeat>(
                action_rtc_unPubScreen,
                seat
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                false,
                null
            )
        }
    }


    fun sendMicrophoneStatus(seat: UserMicSeat) {
        if (!isNeedSend) {
            return
        }
        if (seat.isMySeat()) {
            val textMsg = RtmTextMsg<UserMicSeat>(
                action_rtc_microphoneStatus,
                seat
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                false,
                null
            )
        }
    }

    fun sendCameraStatus(seat: UserMicSeat) {
        if (!isNeedSend) {
            return
        }
        if (seat.isMySeat()) {
            val textMsg = RtmTextMsg<UserMicSeat>(
                action_rtc_cameraStatus,
                seat
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                false,
                null
            )
        }
    }

    fun <T> kickOutFromMicSeat(userMicSeat: UserMicSeatMsg<T>, rtmCallBack: RtmCallBack) {
        if (!isNeedSend) {
            rtmCallBack.onSuccess()
            return
        }
        val textMsg = RtmTextMsg(
            action_rtc_kickOutFromMicSeat,
            userMicSeat
        )
        RtmManager.rtmClient.sendChannelMsg(
            textMsg.toJsonString(),
            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
            true,
            rtmCallBack
        )
    }



    fun kickOutFromRoom(uid: String, msg: String, rtmCallBack: RtmCallBack) {
        if (!isNeedSend) {
            rtmCallBack.onSuccess()
            return
        }
        val textMsg = RtmTextMsg(
            action_rtc_kickOutFromRoom,
            UidAndMsg(uid, msg)
        )
        RtmManager.rtmClient.sendChannelMsg(
            textMsg.toJsonString(),
            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
            true,
            rtmCallBack
        )
    }

    fun sendLockMic(seat: UserMicSeat, callBack: RtmCallBack) {
        if (!isNeedSend) {
            callBack.onSuccess()
            return
        }
        val textMsg = RtmTextMsg<UserMicSeat>(
            action_rtc_lockSeat,
            seat
        )
        RtmManager.rtmClient.sendChannelMsg(
            textMsg.toJsonString(),
            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
            true,
            callBack
        )
    }

    suspend fun sitDown(userMicSeat: UserMicSeat) = suspendCoroutine<Unit> { continuation ->
        if (!isNeedSend) {
            continuation.resume(Unit)
        } else {
            val textMsg = RtmTextMsg<UserMicSeat>(
                action_rtc_sitDown,
                userMicSeat
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                false,
                object : RtmCallBack {
                    override fun onSuccess() {
                        continuation.resume(Unit)
                    }

                    override fun onFailure(code: Int, msg: String) {
                        continuation.resumeWithException(RtmException(code, msg))
                    }
                }
            )
        }
    }

    suspend fun sitUp(userMicSeat: UserMicSeat) = suspendCoroutine<Unit> { continuation ->
        if (!isNeedSend) {
            continuation.resume(Unit)
        } else {
            val textMsg = RtmTextMsg<UserMicSeat>(
                action_rtc_sitUp,
                userMicSeat
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                false,
                object : RtmCallBack {
                    override fun onSuccess() {
                        continuation.resume(Unit)
                    }

                    override fun onFailure(code: Int, msg: String) {
                        continuation.resumeWithException(RtmException(code, msg))
                    }
                }
            )
        }
    }

    suspend fun userJoin(userExtension: UserExtension) = suspendCoroutine<Unit> { continuation ->
        if (!isNeedSend) {
            continuation.resume(Unit)
        } else {
            val textMsg = RtmTextMsg<UserExtension>(
                action_rtc_userJoin,
                userExtension
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                true,
                object : RtmCallBack {
                    override fun onSuccess() {
                        continuation.resume(Unit)
                    }

                    override fun onFailure(code: Int, msg: String) {
                        continuation.resumeWithException(RtmException(code, msg))
                    }
                }
            )
        }
    }

    suspend fun userLeft(userExtension: UserExtension) = suspendCoroutine<Unit> { continuation ->
        if (!isNeedSend) {
            continuation.resume(Unit)
        } else {
            val textMsg = RtmTextMsg<UserExtension>(
                action_rtc_userLeft,
                userExtension
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                true,
                object : RtmCallBack {
                    override fun onSuccess() {
                        continuation.resume(Unit)
                    }

                    override fun onFailure(code: Int, msg: String) {
                        continuation.resumeWithException(RtmException(code, msg))
                    }
                }
            )
        }
    }

    //禁止开麦克风
    fun forbiddenMicSeatAudio(
        forbiddenMicSeatMsg: ForbiddenMicSeatMsg,
        callBack: RtmCallBack
    ) {
        if (!isNeedSend) {
            callBack.onSuccess()
            return
        }
        val textMsg = RtmTextMsg<ForbiddenMicSeatMsg>(
            action_rtc_forbiddenAudio,
            forbiddenMicSeatMsg
        )
        RtmManager.rtmClient.sendChannelMsg(
            textMsg.toJsonString(),
            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
            true,
            callBack
        )
    }

    //禁止开摄像头
    fun forbiddenMicSeatVideo(
        forbiddenMicSeatMsg: ForbiddenMicSeatMsg,
        callBack: RtmCallBack
    ) {
        if (!isNeedSend) {
            callBack.onSuccess()
            return
        }
        val textMsg = RtmTextMsg<ForbiddenMicSeatMsg>(
            action_rtc_forbiddenVideo,
            forbiddenMicSeatMsg
        )
        RtmManager.rtmClient.sendChannelMsg(
            textMsg.toJsonString(),
            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
            true,
            callBack
        )
    }


    fun sendCustomSeatAction(
        customSeatAction: CustomSeatAction,
        callBack: RtmCallBack
    ) {
        if (!isNeedSend) {
            callBack.onSuccess()
            return
        }
        val textMsg = RtmTextMsg<CustomSeatAction>(
            action_rtc_customSeatAction,
            customSeatAction
        )
        RtmManager.rtmClient.sendChannelMsg(
            textMsg.toJsonString(),
            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
            true,
            callBack
        )
    }


    suspend fun sitDownLinkerMic(userMicSeat: UserMicSeat) =
        suspendCoroutine<Unit> { continuation ->
            if (!isNeedSend) {
                continuation.resume(Unit)
            } else {
                val textMsg = RtmTextMsg<UserMicSeat>(
                    rtc_linker_sitDown,
                    userMicSeat
                )
                RtmManager.rtmClient.sendChannelMsg(
                    textMsg.toJsonString(),
                    RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                    false,
                    object : RtmCallBack {
                        override fun onSuccess() {
                            continuation.resume(Unit)
                        }

                        override fun onFailure(code: Int, msg: String) {
                            continuation.resumeWithException(RtmException(code, msg))
                        }
                    }
                )
            }
        }

    suspend fun sitUpLinkerMic(userMicSeat: UserMicSeat) = suspendCoroutine<Unit> { continuation ->
        if (!isNeedSend) {
            continuation.resume(Unit)
        } else {
            val textMsg = RtmTextMsg<UserMicSeat>(
                rtc_linker_sitUp,
                userMicSeat
            )
            RtmManager.rtmClient.sendChannelMsg(
                textMsg.toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                false,
                object : RtmCallBack {
                    override fun onSuccess() {
                        continuation.resume(Unit)
                    }

                    override fun onFailure(code: Int, msg: String) {
                        continuation.resumeWithException(RtmException(code, msg))
                    }
                }
            )
        }
    }


}