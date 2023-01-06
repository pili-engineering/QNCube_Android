package com.hapi.baseframe.refresh

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.hapi.base_mvvm.R
import kotlin.math.abs

class DefaultLoadView(context: Context) : ILoadView(context) {

    private val mCircleImageView: ImageView
    private val circularProgressDrawable: CircularProgressDrawable
    private val mAttachView: View
    private val tvTipView: TextView
    var defaultHeight: Int = 0

    private fun dp2px(context: Context, dpVal: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dpVal * density + 0.5f).toInt()
    }

    companion object {
        // Default background for the progress spinner
        private const val CIRCLE_BG_LIGHT = -0x50506
    }

    init {
        mAttachView =
            LayoutInflater.from(context).inflate(R.layout.default_loadmore_view, null, false)
        defaultHeight = dp2px(context, 40f)

        mCircleImageView = mAttachView.findViewById(R.id.pbProgressBar)
        tvTipView = mAttachView.findViewById(R.id.tvTip)
        circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.setBackgroundColor(CIRCLE_BG_LIGHT)
        circularProgressDrawable.alpha = 255
        circularProgressDrawable.setColorSchemeColors(
            -0xff6634,
            -0xbbbc,
            -0x996700,
            -0x559934,
            -0x7800
        )
        mCircleImageView.setImageDrawable(circularProgressDrawable)
        // 设置环形的半径(控制环形的尺寸)
        circularProgressDrawable.centerRadius = dp2px(context, 10f).toFloat()
        // 设置环形的宽度
        circularProgressDrawable.strokeWidth = 8f
        // 设置环形的节点显示(Paint.Cap.ROUND即圆角)
        circularProgressDrawable.strokeCap = Paint.Cap.ROUND
        circularProgressDrawable.backgroundColor = Color.parseColor("#00000000")
    }

    override fun checkHideNoMore() {
        onFinishLoad(false)
    }

    override fun getFreshHeight(): Int {
        return defaultHeight
    }
    override fun getAttachView(): View {
        return mAttachView
    }

    override fun onPointMove(totalY: Float, dy: Float): Float {
        if (!isShowLoading && !isShowLoadMore) {
            // 设置绘制进度弧长
            // 设置绘制进度弧长
            circularProgressDrawable.setStartEndTrim(0f, totalY * 0.4f / defaultHeight)
            circularProgressDrawable.progressRotation = totalY / defaultHeight
            // 设置箭头的尺寸
            circularProgressDrawable.setArrowDimensions(8f, 8f)
            circularProgressDrawable.arrowEnabled = true
            // 设置箭头的尺寸
            // 在箭头的尺寸上缩放倍数, 如果没有设置尺寸则无效
            circularProgressDrawable.arrowScale = 2f
        }
        if (dy > 0) {
            val viewH = defaultHeight
            // 8 4 > 10
            val dyNew = if (abs(totalY + dy) > viewH) {
                if (totalY > viewH) {
                    0
                } else {
                    viewH - totalY
                }
            } else {
                dy
            }.toFloat()
            return dyNew
        }
        return dy
    }

    override fun onPointUp(toStartLoad: Boolean) {
        super.onPointUp(toStartLoad)
        if (toStartLoad) {
            circularProgressDrawable.arrowEnabled = false
            circularProgressDrawable.start()
        } else {
            circularProgressDrawable.stop()
        }
    }

    override fun onFinishLoad(showNoMore: Boolean) {
        super.onFinishLoad(showNoMore)
        circularProgressDrawable.stop()
        if (showNoMore) {
            mCircleImageView.visibility = View.GONE
            tvTipView.visibility = View.VISIBLE
            tvTipView.text = noMoreText
        } else {
            mCircleImageView.visibility = View.VISIBLE
            tvTipView.visibility = View.GONE
            tvTipView.text = ""
        }
    }

}