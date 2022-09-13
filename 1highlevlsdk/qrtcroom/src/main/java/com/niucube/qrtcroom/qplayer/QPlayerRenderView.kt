package com.niucube.qrtcroom.qplayer

import android.view.Surface
import android.view.View

/**
 * 观众播放器预览
 * 子类 QPlayerTextureRenderView 和 QSurfaceRenderView
 */
interface QPlayerRenderView {
    /**
     * 设置预览模式
     *
     * @param previewMode 预览模式枚举
     */
    fun setDisplayAspectRatio(previewMode: PreviewMode)
    fun setRenderCallback(rendCallback: QRenderCallback?)
    fun getView(): View?
    fun getSurface(): Surface?
}