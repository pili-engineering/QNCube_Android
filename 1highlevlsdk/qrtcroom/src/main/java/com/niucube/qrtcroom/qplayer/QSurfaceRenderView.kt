package com.niucube.qrtcroom.qplayer

import android.content.Context
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import com.pili.pldroid.player.common.ViewMeasurer

class QSurfaceRenderView : FrameLayout,QPlayerRenderView {

    private lateinit var mRenderView: PLSurfaceRenderView
    private var mSurface: Surface? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mRenderView = PLSurfaceRenderView(context)
        val lp = LayoutParams(-1, -1, 17)
        this.mRenderView.layoutParams = lp
        this.addView(this.mRenderView)
        this.isFocusable = true
        this.isFocusableInTouchMode = true
        this.requestFocus()
    }

    private var mDisplayAspectRatio = PreviewMode.ASPECT_RATIO_FIT_PARENT
    private var mVideoWidth = 0
    private var mVideoHeight = 0

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

    fun setZOrderOnTop(onTop: Boolean) {
        this.mRenderView.setZOrderOnTop(onTop)
    }

    fun setZOrderMediaOverlay(overlay: Boolean) {
        this.mRenderView.setZOrderMediaOverlay(overlay)
    }

    fun setVideoSize(width: Int, height: Int) {
        mVideoWidth = width
        mVideoHeight = height
        mRenderView.setVideoSize(width, height)
    }

    inner class PLSurfaceRenderView : SurfaceView {
        constructor(context: Context) : this(context, null)
        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            this.holder.addCallback(mSurfaceCallback)
        }

        private val mSurfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mSurface = holder.surface
                if (mQRenderCallback != null) {
                    mQRenderCallback!!.onSurfaceCreated(holder.surface, 0, 0)
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                mSurface = holder.surface
                if (mQRenderCallback != null) {
                    mQRenderCallback!!.onSurfaceChanged(holder.surface, width, height)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mSurface = null
                if (mQRenderCallback != null) {
                    mQRenderCallback!!.onSurfaceDestroyed(holder.surface)
                }
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val dimension = ViewMeasurer.measure(
                mDisplayAspectRatio.intValue, widthMeasureSpec, heightMeasureSpec,
                mVideoWidth,
                mVideoHeight, 6789, -1, -1
            )
            setMeasuredDimension(dimension.viewWidth, dimension.viewHeight)
        }

        fun setVideoSize(width: Int, height: Int) {
            this.holder.setFixedSize(width, height)
            requestLayout()
        }
    }
}