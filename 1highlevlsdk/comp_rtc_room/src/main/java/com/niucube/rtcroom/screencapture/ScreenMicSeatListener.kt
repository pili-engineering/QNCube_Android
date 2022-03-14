package com.niucube.rtcroom.screencapture

import com.niucube.absroom.seat.ScreenMicSeat

//屏幕共享监听
interface   ScreenMicSeatListener{
    fun onScreenMicSeatAdd(seat: ScreenMicSeat)
    fun onScreenMicSeatRemove(seat: ScreenMicSeat)
}