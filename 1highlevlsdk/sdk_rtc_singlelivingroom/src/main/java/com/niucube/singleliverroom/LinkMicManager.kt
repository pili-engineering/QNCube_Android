package com.niucube.singleliverroom

import com.niucube.absroom.AudioTrackParams
import com.niucube.absroom.RtcOperationCallback
import com.niucube.absroom.VideoTrackParams
import com.niucube.absroom.seat.UserExtension
import com.niucube.absroom.seat.UserMicSeat

interface LinkMicManager {

    fun userClientTypeSyncMicSeats(micSeats: List<UserMicSeat>)

    fun setLinkMicSeatListener(lister: LinkMicSeatListener)

    /**
     * 上麦
     *
     */
    fun sitDown(
        userExt: UserExtension,
        videoTrackParams: VideoTrackParams?,
        audioTrackParams: AudioTrackParams?,
        callBack: RtcOperationCallback
    )

    /**
     * 下麦
     * @param seat
     */
    fun sitUp(callBack: RtcOperationCallback)

    //踢麦
    fun kickOutFromMicSeat(seat: UserMicSeat, msg: String,callBack: RtcOperationCallback)




}