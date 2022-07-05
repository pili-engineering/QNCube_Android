package com.niucube.basemutableroom.screencapture

import com.niucube.basemutableroom.absroom.seat.ScreenMicSeat


//屏幕共享监听
interface   ScreenMicSeatListener{
    fun onScreenMicSeatAdd(seat: ScreenMicSeat)
    fun onScreenMicSeatRemove(seat: ScreenMicSeat)
}