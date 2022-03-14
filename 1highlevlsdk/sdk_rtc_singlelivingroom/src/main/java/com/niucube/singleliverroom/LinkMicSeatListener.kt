package com.niucube.singleliverroom

import com.niucube.absroom.seat.UserMicSeat


interface LinkMicSeatListener{
    /**
     * 有连麦用户加入
     * @param seat
     */
    fun onMicLinkerSitDown( seat: UserMicSeat);

    /**
     * 有连麦用户下麦
     * @param seat
     */
    fun onMicLinkerSitUp(seat:UserMicSeat,isOffLine:Boolean);

    /**
     * 麦位麦克风变化
     * @param seat
     * @param isOpen
     */
    fun onMicAudioStatusChange(seat:UserMicSeat);

    /**
     * 麦位摄像头变化
     * @param seat
     * @param isOpen
     */
    fun onCameraStatusChange( seat:UserMicSeat);

    /**
     * 连麦用户被踢
     */
    fun onKickOutFromMicSeat( seat:UserMicSeat)

}