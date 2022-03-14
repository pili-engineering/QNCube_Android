package com.niucube.rtcroom.customtrack


interface VideoChanel {
    fun sendVideoFrame(videoData: ByteArray, width: Int, height: Int)
}