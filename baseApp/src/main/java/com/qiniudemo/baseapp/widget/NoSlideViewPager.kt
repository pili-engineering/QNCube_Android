package com.qiniudemo.baseapp.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NoSlideViewPager : ViewPager{
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }
    var isSlideAble = false
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
       if(isSlideAble){
           return super.onTouchEvent(ev)
       }else{
           return false
       }
    }

}