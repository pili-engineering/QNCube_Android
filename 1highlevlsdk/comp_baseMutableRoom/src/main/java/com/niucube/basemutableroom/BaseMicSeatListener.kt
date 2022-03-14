package com.niucube.basemutableroom

//麦回调
interface BaseMicSeatListener<T:BaseMutableMicSeat> {

    //主播上麦 参数用户麦位
    fun onUserSitDown(micSeat:T)
    //主播下麦
    fun onUserSitUp(micSeat:T,isOffLine:Boolean)
    //麦上视频状态变化
    fun onCameraStatusChanged(micSeat:T)

    fun onMicAudioStatusChanged(micSeat:T)

    fun onSyncMicSeats(seats: List<T>){}
}