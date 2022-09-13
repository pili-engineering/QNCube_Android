package com.niucube.qrtcroom.customtrack


interface VideoChannel {
    fun sendVideoFrame(videoData: ByteArray, width: Int, height: Int)
}