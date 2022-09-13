package com.niucube.player.video.floating

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.niucube.player.PlayerStatus.MODE_TINY_WINDOW
import com.niucube.player.utils.PalyerUtil

internal class TinyFloatView : FrameLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var startX = 0f
    private var startY = 0f
    var parentWindType: (() -> Int)? = null

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {
            startX = event.rawX
            startY = event.rawY
            return super.onInterceptTouchEvent(event)
        }

        if (event.action == MotionEvent.ACTION_MOVE && parentWindType?.invoke() == MODE_TINY_WINDOW) {
            val x = event.rawX - startX
            val y = event.rawY - startY

            return Math.abs(x) > 20 || Math.abs(y) > 20
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (parentWindType?.invoke() != MODE_TINY_WINDOW) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {

                val x = event.rawX - startX
                val y = event.rawY - startY
                val screenWidth = PalyerUtil.getScreenWidth(context)
                val scrrenHeight = PalyerUtil.getScreenHeight(context)
                startX = event.rawX
                startY = event.rawY

                if (getX() + x < 0 || getX() + x + width > screenWidth) {
                    return true
                }
                if (getY() + y < 0 || getY() + y + height > scrrenHeight) {
                    return true
                }

                updateWindowPos(x, y)
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateWindowPos(x: Float, y: Float) {
        if (x == 0f && y == 0f) {
            return
        }
        translationX += x
        translationY += y
    }

    fun resetTranslation() {
        translationX = 0f
        translationY = 0f
    }
}