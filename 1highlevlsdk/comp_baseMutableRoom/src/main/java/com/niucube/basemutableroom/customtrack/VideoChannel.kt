package com.niucube.basemutableroom.customtrack


interface VideoChannel {
    fun sendVideoFrame(videoData: ByteArray, width: Int, height: Int)
}