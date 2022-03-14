package com.niucube.rtcroom.screencapture

import androidx.fragment.app.FragmentActivity
import com.niucube.absroom.ScreenTrackParams
import com.qiniu.droid.rtc.QNTextureView
import com.qiniu.droid.rtc.QNTrack

interface ScreenShareManager {

    //发布屏幕共享
    fun pubLocalScreenTrack(params: ScreenTrackParams)
    fun pubLocalScreenTrackWithPermissionCheck(
        activity: FragmentActivity,
        callback: ScreenCapturePlugin.ScreenCaptureListener,
        params: ScreenTrackParams = ScreenTrackParams()
    )
    //取消屏幕共享
    fun unPubLocalScreenTrack()
    //添加屏幕共享监听
    fun addScreenMicSeatListener(listener: ScreenMicSeatListener)
    fun removeScreenMicSeatListener(listener: ScreenMicSeatListener)
    //设置屏幕共享预览
    fun setUserScreenWindowView(uid:String,view: QNTextureView)
    fun getUserScreenTrackInfo(uid:String): QNTrack?


}