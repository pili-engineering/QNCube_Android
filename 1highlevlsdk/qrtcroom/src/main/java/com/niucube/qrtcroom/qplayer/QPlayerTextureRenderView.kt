package com.niucube.qrtcroom.qplayer

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import com.niucube.qrtcroom.liblog.QLiveLogUtil
import com.pili.pldroid.player.common.ViewMeasurer

open class QPlayerTextureRenderView : FrameLayout,QPlayerRenderView {

    private lateinit var mRenderView: PLTextureRenderView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.mRenderView = PLTextureRenderView(context)
        val lp = LayoutParams(-1, -1, 17)
        this.mRenderView.layoutParams = lp
        this.addView(this.mRenderView)
        this.isFocusable = true
        this.isFocusableInTouchMode = true
        this.requestFocus()
    }

    private var mDisplayAspectRatio = PreviewMode.ASPECT_RATIO_PAVED_PARENT
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurface: Surface? = null

     open fun setVideoSize(width: Int, height: Int) {
        mVideoWidth = width
        mVideoHeight = height
        Log.d("QPlayerTexture", "setVideoSize ${width} ${height}")
        mRenderView.requestLayout()
        requestLayout()
    }

    internal fun stopPlayback() {
        releaseSurfaceTexture()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    private fun releaseSurfaceTexture() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture!!.release()
            mSurfaceTexture = null
        }
    }

    private var mQRenderCallback: QRenderCallback? = null
    override fun setRenderCallback(rendCallback: QRenderCallback?) {
        mQRenderCallback = rendCallback
    }

    override fun setDisplayAspectRatio(previewMode: PreviewMode) {
        mDisplayAspectRatio = previewMode
        val childView = getChildAt(0)
        childView?.requestLayout()
    }

    override fun getView(): View {
        return this
    }

    override fun getSurface(): Surface? {
        return mSurface
    }
    fun setMirror(mirror: Boolean) {
        this.scaleX = if (mirror) -1.0f else 1.0f
    }
    private inner class PLTextureRenderView : TextureView {
        constructor(context: Context) : this(context, null)
        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            surfaceTextureListener = mTextureListener
        }

        private val mTextureListener: TextureView.SurfaceTextureListener = object :
            TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                if (mSurfaceTexture != null) {
                    setSurfaceTexture(mSurfaceTexture!!)
                    surfaceTexture.release()
                    QLiveLogUtil.d("PLVideoTextureView", "onSurfaceTextureAvailable: replace surface")
                } else {
                    mSurfaceTexture = surfaceTexture
                    mSurface =
                        Surface(mSurfaceTexture)
                    QLiveLogUtil.d("PLVideoTextureView", "onSurfaceTextureAvailable: new surface")
                }
                if (mQRenderCallback != null) {
                    QLiveLogUtil.d("PLVideoTextureView", "onSurfaceTextureAvailable: onSurfaceCreated")
                    mQRenderCallback!!.onSurfaceCreated(
                        mSurface!!,
                        width,
                        height
                    )
                }
            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                if (mQRenderCallback != null) {
                    mQRenderCallback!!.onSurfaceChanged(
                        mSurface!!,
                        width,
                        height
                    )
                }
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                if (mQRenderCallback != null) {
                    mQRenderCallback!!.onSurfaceDestroyed(mSurface!!)
                    QLiveLogUtil.d("PLVideoTextureView", "onSurfaceTextureDestroyed")
                }
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val dimension = ViewMeasurer.measure(
                mDisplayAspectRatio.intValue, widthMeasureSpec, heightMeasureSpec,
                mVideoWidth,
                mVideoHeight, 6789, -1, -1
            )
            setMeasuredDimension(dimension.viewWidth, dimension.viewHeight)
        }
    }
}