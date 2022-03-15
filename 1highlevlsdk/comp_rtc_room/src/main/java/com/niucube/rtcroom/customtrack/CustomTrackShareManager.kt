package com.niucube.rtcroom.customtrack

import com.niucube.absroom.AudioTrackParams
import com.niucube.absroom.VideoTrackParams
import com.qiniu.droid.rtc.QNTextureView
import com.qiniu.droid.rtc.QNTrack

interface CustomTrackShareManager{

    fun getUserExtraTrackInfo(tag:String,uid:String): QNTrack?
    //发布自定义视频视频轨道
    fun pubCustomVideoTrack(trackTag:String, params: VideoTrackParams): VideoChannel
    // 发布自定义音频轨道 暂时不支持
    fun pubCustomAudioTrack(trackTag:String, params: AudioTrackParams)
    fun unPubCustomTrack(trackTag:String)
    //添加定义轨道事件
    fun addCustomMicSeatListener(listener: CustomMicSeatListener)
    fun removeCustomMicSeatListener(listener: CustomMicSeatListener)
    //设置自定义轨道预览
    fun setUserCustomVideoPreview(trackTag:String,uid:String,view: QNTextureView)
}