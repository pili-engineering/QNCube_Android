package com.niucube.absroom.seat

import com.niucube.comproom.RoomManager
import com.niucube.comproom.provideMeId

open class MicSeat {
    //用户ID
    open var uid = ""

    fun isMySeat(): Boolean{
        if (RoomManager.mCurrentRoom == null) {
            return false;
        }
        return uid == RoomManager.mCurrentRoom?.provideMeId()
    }
}