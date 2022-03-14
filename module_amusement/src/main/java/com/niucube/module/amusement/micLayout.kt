package com.niucube.module.amusement

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.hapi.ut.ScreenUtil

//
////效果图
//val uiDesignSketchWidth = 414
//val uiDesignSketchHeight = 736
//
//
//class Point(var x:Int,val y:Int)
////效果图上的每个麦位
//val uiDesignSketchMicSeat by lazy {
//    listOf<Point>(
//        Point(147, 97),
//
//        Point(32, 235),
//        Point(152, 235),
//        Point(272, 235),
//
//        Point(32, 355),
//        Point(152, 355),
//        Point(272, 355),
//
//        Point(32, 475),
//    )
//}
//
////屏幕宽 / 高
//var screenWidth = ScreenUtil.getScreenWidth()
//var screenHeight = ScreenUtil.getScreenHeight()
//
////屏幕宽高比例
//val screenWidthRatio by lazy { screenWidth / uiDesignSketchWidth }
//val screenHeightRatio by lazy { screenHeight / uiDesignSketchHeight }
//
//class ItemLocation(var left: Int, var top: Int, var right: Int, var bottom: Int)
//
////布局中每个麦位的坐标
//val micSeatLayoutParams by lazy {
//    ArrayList<ItemLocation>().apply {
//        //循环UI图上的每个麦位
//        for (i in uiDesignSketchMicSeat.indices) {
//            val left = (uiDesignSketchMicSeat[i].x * screenWidthRatio).toInt()
//            val top = (uiDesignSketchMicSeat[i].y * screenHeightRatio).toInt()
//            val right = left + if(i==0){120}else{110} * screenWidthRatio
//            val bottom = top +  if(i==0){120}else{110} * screenWidthRatio
//            add(ItemLocation(left, top, right.toInt(), bottom.toInt()))
//        }
//    }
//}
//
//class MicSeatLayoutManager : RecyclerView.LayoutManager() {
//
//    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
//
//        return RecyclerView.LayoutParams(
//            RecyclerView.LayoutParams.WRAP_CONTENT,
//            RecyclerView.LayoutParams.WRAP_CONTENT
//        )
//    }
//
//
//    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
//        if (itemCount <= 0) {
//            return
//        }
//        detachAndScrapAttachedViews(recycler)
//
//        //计算每个item的位置信息,存储在itemFrames里面
//        for (i in 0 until itemCount) {
//            //从缓存中取出
//            val view = recycler.getViewForPosition(i)
//            //添加到RecyclerView中
//            addView(view)
//            val item = micSeatLayoutParams[i]
//            //测量
//            measureChildWithMargins(view, item.right - item.left, item.bottom - item.top)
//
//            Log.d(
//                "MicSeatLayoutManager",
//                "onLayoutChildren ${i}  ${item.left}  ${item.top}  ${item.right}  ${item.bottom}   "
//            )
//
//            layoutDecorated(view, item.left, item.top, item.right, item.bottom)
//        }
//    }
//
//    override fun isAutoMeasureEnabled(): Boolean {
//        return true
//    }
//
//    override fun canScrollVertically(): Boolean {
//        return false
//    }
//
//    override fun canScrollHorizontally(): Boolean {
//        return false
//    }
//
//}
//
//
//
