package com.niucube.rtcroom.customtrack


interface VideoChannel {
    fun sendVideoFrame(videoData: ByteArray, width: Int, height: Int)
}