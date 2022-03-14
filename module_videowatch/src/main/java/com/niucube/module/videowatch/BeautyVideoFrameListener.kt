package com.niucube.module.videowatch

import androidx.lifecycle.*
//import com.niucube.compui.beauty.SenseTimePluginManager
import com.niucube.rtm.RtmManager
import com.qiniu.droid.rtc.QNVideoFrameListener
import com.qiniu.droid.rtc.QNVideoFrameType

class BeautyVideoFrameListener : QNVideoFrameListener, LifecycleEventObserver {

    override fun onYUVFrameAvailable(
        data: ByteArray,
        type: QNVideoFrameType,
        width: Int,
        height: Int,
        rotation: Int,
        timestampNs: Long
    ) {

    }

    private var lastRotation = -1000
    override fun onTextureFrameAvailable(
        textureID: Int,
        type: QNVideoFrameType,
        width: Int,
        height: Int,
        rotation: Int,
        timestampNs: Long,
        transformMatrix: FloatArray?
    ): Int {
////        return textureID
//        if (SenseTimePluginManager.mGLHandler == null) {
//            SenseTimePluginManager.onCaptureStarted()
//        }
//
//        SenseTimePluginManager.updateDirection(rotation)
//
//        return SenseTimePluginManager.onRenderingFrame(
//            type == QNVideoFrameType.TEXTURE_OES,
//            textureID,
//            width,
//            height
//        )
        return 0;
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
         //   SenseTimePluginManager.release()
        }
    }
}