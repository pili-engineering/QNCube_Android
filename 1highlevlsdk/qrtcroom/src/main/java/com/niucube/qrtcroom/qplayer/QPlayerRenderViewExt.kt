package com.niucube.qrtcroom.qplayer

import android.view.Surface


interface QRenderCallback {
    fun onSurfaceCreated(var1: Surface, var2: Int, var3: Int)
    fun onSurfaceChanged(var1: Surface, var2: Int, var3: Int)
    fun onSurfaceDestroyed(var1: Surface)
}

enum class PreviewMode(val intValue: Int) {
    ASPECT_RATIO_ORIGIN(0),
    ASPECT_RATIO_FIT_PARENT(1),
    ASPECT_RATIO_PAVED_PARENT(2),
    ASPECT_RATIO_16_9(3),
    ASPECT_RATIO_4_3(4),
}