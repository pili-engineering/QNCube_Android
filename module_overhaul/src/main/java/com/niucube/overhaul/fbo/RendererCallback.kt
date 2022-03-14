package com.niucube.overhaul.fbo
import android.opengl.GLES20
import java.nio.ByteBuffer

/**
 * 渲染完毕的回调
 */
interface RendererCallback {
    /**
     * 渲染完毕
     *
     * @param data   缓存数据
     * @param width  数据宽度
     * @param height 数据高度
     */
    fun onRendererDone(data: ByteBuffer, width: Int, height: Int)
}


