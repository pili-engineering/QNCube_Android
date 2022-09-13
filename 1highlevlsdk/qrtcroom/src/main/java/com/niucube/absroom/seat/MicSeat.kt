package com.niucube.absroom.seat
open class MicSeat {
    //用户ID
    open var uid = ""
    fun isMySeat(meId: String): Boolean {
        return uid == meId
    }
}