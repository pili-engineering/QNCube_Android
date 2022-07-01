package com.niucube.lazysitmutableroom

import com.niucube.basemutableroom.BaseMicSeatListener
import com.niucube.basemutableroom.absroom.seat.MicSeat

interface UserMicSeatListener : BaseMicSeatListener<LazySitUserMicSeat>{

    //麦位被管理员禁麦变化
    fun onVideoForbiddenStatusChanged(seat: LazySitUserMicSeat, msg: String){}
    fun onAudioForbiddenStatusChanged(seat: LazySitUserMicSeat, msg: String){}
    //  从麦位上踢出
    fun onKickOutFromMicSeat(seat: LazySitUserMicSeat, msg: String){}
    // 从房间踢出
    fun onKickOutFromRoom(userId: String, msg: String){}
    //自定义麦位信令
    fun onCustomSeatAction(seat: MicSeat, key: String, values: String){}

}