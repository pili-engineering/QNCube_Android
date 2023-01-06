package com.hapi.baseframe.refresh

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs

class DefaultRefreshView(context: Context) : IRefreshView(context) {

    companion object {
        private const val CIRCLE_BG_LIGHT = -0x50506
    }

    private fun dp2px(context: Context, dpVal: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dpVal * density + 0.5f).toInt()
    }

    private var circularProgressDrawable: CircularProgressDrawable
    private var mCircleView: CircleImageView
    private var mCircleDiameter: Int = 0
    private var maxScrollHeight = 0
    private var reFreshTopHeight = 0

    init {
        maxScrollHeight = dp2px(context, 65f)
        reFreshTopHeight = dp2px(context, 10f)
        mCircleDiameter = dp2px(context, 45f)
        mCircleView = CircleImageView(context, CIRCLE_BG_LIGHT)
        circularProgressDrawable = CircularProgressDrawable(context)
        mCircleView.size = dp2px(context, 40f)
        mCircleView.setImageDrawable(circularProgressDrawable)

        circularProgressDrawable.setColorSchemeColors(
            -0xff6634,
            -0xbbbc,
            -0x996700,
            -0x559934,
            -0x7800
        )
        circularProgressDrawable.alpha = 255
        circularProgressDrawable.setStyle(CircularProgressDrawable.DEFAULT)
        // 设置环形的宽度
        circularProgressDrawable.strokeWidth = 8f
        // 设置环形的节点显示(Paint.Cap.ROUND即圆角)
        circularProgressDrawable.strokeCap = Paint.Cap.ROUND
        circularProgressDrawable.backgroundColor = Color.parseColor("#00000000")
    }

    override fun getFreshTopHeight(): Int {
        return reFreshTopHeight
    }

    override fun getFreshHeight(): Int {
        return mCircleDiameter
    }

    override fun getAttachView(): View {
        return mCircleView
    }

    override fun isFloat(): Boolean {
        return true
    }

    private val decelerateInterpolator = DecelerateInterpolator()
    override fun onPointMove(totalY: Float, dy: Float): Float {
        // 启用箭头
        circularProgressDrawable.arrowEnabled = true

        // 设置绘制进度弧长
        circularProgressDrawable.setStartEndTrim(0f, -totalY * 0.4f / maxScrollHeight)
        circularProgressDrawable.progressRotation = -totalY / maxScrollHeight
        // 设置箭头的尺寸
        circularProgressDrawable.setArrowDimensions(
            4f + 4 * Math.abs(totalY / maxScrollHeight),
            4f + 4 * Math.abs(totalY / maxScrollHeight)
        )
        // 设置环形的半径(控制环形的尺寸)
        circularProgressDrawable.centerRadius = 22f
        // 在箭头的尺寸上缩放倍数, 如果没有设置尺寸则无效
        circularProgressDrawable.arrowScale = 2f
        val alpha = 0.2f + Math.abs(totalY / (maxScrollHeight + mCircleDiameter))

        mCircleView.alpha = (alpha)

        val scale = decelerateInterpolator.getInterpolation(
            Math.min(
                maxScrollHeight.toFloat(),
                Math.abs(totalY)
            ) / maxScrollHeight
        )
        mCircleView.scaleY = scale
        mCircleView.scaleX = scale
        Log.d(
            "onPointMove",
            " alpha ${alpha}   ${totalY} ${(maxScrollHeight + mCircleDiameter)} "
        )
        if (dy < 0) {
            //下拉
            val maxHeight = maxScrollHeight + mCircleDiameter
            val dyNew = if (abs(dy + totalY) > maxHeight) {
                // -5  -6  > 10
                if (-totalY > maxHeight) {
                    0
                } else {
                    -(maxHeight + totalY)
                }
            } else {
                dy
            }.toFloat()
            if (Math.abs(totalY) > mCircleDiameter) {
                val ratio =
                    decelerateInterpolator.getInterpolation((Math.abs(totalY) - mCircleDiameter) / (maxScrollHeight))
                Log.d(
                    "onPointMove",
                    " ratio ${ratio}   ${totalY} ${(maxScrollHeight + mCircleDiameter)} "
                )
                return (1 - ratio) * dyNew
            } else {
                return dyNew
            }
        }
        return dy
    }

    override fun onPointUp(toStartRefresh: Boolean) {
        mCircleView.scaleY = 1f
        mCircleView.scaleX = 1f
        if (toStartRefresh) {
            circularProgressDrawable.arrowEnabled = false
            circularProgressDrawable.alpha = 255
            mCircleView.alpha = 1f
            circularProgressDrawable.start()
        } else {
            circularProgressDrawable.arrowEnabled = true
            circularProgressDrawable.stop()
        }
    }

    override fun onFinishRefresh() {
        circularProgressDrawable.stop()
    }
}