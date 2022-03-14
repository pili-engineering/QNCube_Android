package com.niucube.singleliverroom

import com.niucube.absroom.seat.UserExtension
import com.niucube.absroom.seat.UserMicSeat

interface LivingListener {
    /**
     * 主播上麦
     * @param seat
     */
    fun onLiverSitDown(seat: UserMicSeat);

    /**
     * 主播下麦
     * @param seat
     */

    fun onLiverSitUp(seat: UserMicSeat, isOffLine: Boolean);

    /**
     * 麦位麦克风变化
     * @param seat
     * @param isOpen
     */
    fun onLiverAudioStatusChange(seat: UserMicSeat);

    /**
     * 麦位摄像头变化
     * @param seat
     * @param isOpen
     */
    fun onLiverCameraStatusChange(seat: UserMicSeat);

    /**
     * 用户退出房间
     *
     */
    fun onKickOutFromRoom(userId: String,msg:String)

}