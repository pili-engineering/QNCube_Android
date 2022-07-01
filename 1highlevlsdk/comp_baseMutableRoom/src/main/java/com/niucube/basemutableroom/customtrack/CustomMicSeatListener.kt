package com.niucube.basemutableroom.customtrack

import com.niucube.basemutableroom.absroom.seat.CustomMicSeat

//自定义轨道监听
interface   CustomMicSeatListener{
    fun onCustomMicSeatAdd(seat: CustomMicSeat)
    fun onCustomMicSeatRemove(seat: CustomMicSeat)
}
