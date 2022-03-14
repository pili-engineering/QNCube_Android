package com.niucube.overhaul.fbo
import android.opengl.GLES30


object OpenGLTools {

    var textures:IntArray?=null
    fun createFBOTexture(width: Int, height: Int): Int {
        // 新建纹理ID
        textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)

        // 绑定纹理ID
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures!![0])

        // 根据颜色参数，宽高等信息，为上面的纹理ID，生成一个2D纹理
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height,
            0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)

        // 设置纹理边缘参数
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,GLES30.GL_CLAMP_TO_EDGE.toFloat())

        // 解绑纹理ID
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0)
        return textures!![0]
    }

    var fbs:IntArray?=null
    fun createFrameBuffer(): Int {
         fbs = IntArray(1)
        GLES30.glGenFramebuffers(1, fbs, 0)
        bindFBO()
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures!![0])
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D,textures!![0], 0);
        // 解绑纹理ID
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0)
        return fbs!![0]

    }

    fun bindFBO() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,  fbs!![0])

    }

    fun unbindFBO() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }

    fun deleteFBO() {
        //删除Frame Buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
        GLES30.glDeleteFramebuffers(1, fbs, 0)
        //删除纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        GLES30.glDeleteTextures(1, textures, 0)
    }
}