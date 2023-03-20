package com.qiniudemo.baseapp.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewParent
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.viewpager.widget.ViewPager

class QWebView : WebView {

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    var anchorView:ViewGroup?=null
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val viewParent = findViewParentIfNeeds()
            viewParent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.onTouchEvent(event)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        Log.d("webview","onOverScrolled scrollX=" + scrollX + ";scrollY=" + scrollY
                + ";clampedX=" + clampedX + ";clampedY=" + clampedY)
        if (clampedX) {
            val viewParent = findViewParentIfNeeds()
            viewParent?.requestDisallowInterceptTouchEvent(false)
        }
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
    }

    private fun findViewParentIfNeeds(): ViewParent? {
        val parent = anchorView

        if (parent == null) {
            return parent
        }
        return if (parent is ViewPager ||
            parent is AbsListView ||
            parent is ScrollView ||
            parent is HorizontalScrollView
        ) {
            parent
        } else {
           return null
        }
    }
}