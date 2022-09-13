package com.niucube.module.amusement

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.qiniu.droid.rtc.QNRenderMode
import com.qiniu.droid.rtc.QNTranscodingLiveStreamingTrack

//效果图
val uiDesignSketchWidth = 414
val uiDesignSketchHeight = 736

val mixRatio = 1.5


class Point(var x: Int, val y: Int)

//效果图上的每个麦位
val uiDesignSketchMicSeat by lazy {
    listOf<Point>(
        Point(147, 97),

        Point(32, 235),
        Point(152, 235),
        Point(272, 235),

        Point(32, 355),
        Point(152, 355),
        Point(272, 355),

        Point(32, 475),
    )
}

//屏幕宽 / 高
var screenWidth = 0//ScreenUtil.getScreenWidth()
var screenHeight = 0// ScreenUtil.getScreenHeight()
val micSeatLayoutParams = ArrayList<ItemLocation>()

//屏幕宽高比例
var screenWidthRatio = 0.0//by lazy { screenWidth / uiDesignSketchWidth }
var screenHeightRatio = 0.0// by lazy { screenHeight / uiDesignSketchHeight }

fun initWH(view: View, w: Int, h: Int) {

    val scaleCenter = {
        //居中缩放方案
        val wr = w / uiDesignSketchWidth.toDouble()
        val hr = h / uiDesignSketchHeight.toDouble()
        val ratio = Math.min(wr, hr)
        screenWidth = (uiDesignSketchWidth * ratio).toInt()
        screenHeight = ((uiDesignSketchHeight * ratio).toInt())

        val lp = view.layoutParams
        lp.width = screenWidth
        lp.height = screenHeight
        view.layoutParams = lp
    }

    scaleCenter.invoke()

    //屏幕宽高比例
    screenWidthRatio = (screenWidth / uiDesignSketchWidth.toDouble())
    screenHeightRatio = (screenHeight / uiDesignSketchHeight.toDouble())
    //循环UI图上的每个麦位
    micSeatLayoutParams.clear()
    for (i in uiDesignSketchMicSeat.indices) {
        val left = (uiDesignSketchMicSeat[i].x * screenWidthRatio).toInt()
        val top = (uiDesignSketchMicSeat[i].y * screenHeightRatio).toInt()
        val right = left + if (i == 0) {
            120
        } else {
            110
        } * screenWidthRatio
        val bottom = top + if (i == 0) {
            120
        } else {
            110
        } * screenHeightRatio

        micSeatLayoutParams.add(ItemLocation(left, top, right.toInt(), bottom.toInt()))
    }
}

class ItemLocation(var left: Int, var top: Int, var right: Int, var bottom: Int) {
    fun getMergeTrackOption(): QNTranscodingLiveStreamingTrack {
        return QNTranscodingLiveStreamingTrack().apply {
            x = (left / screenWidthRatio * mixRatio).toInt()
            y = (top / screenHeightRatio * mixRatio).toInt()
            zOrder = 0
            width = ((right - left) / screenWidthRatio * mixRatio).toInt()
            height = ((bottom - top) / screenHeightRatio * mixRatio).toInt()
            renderMode = QNRenderMode.FILL
        }
    }
}

class MicSeatLayoutManager : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {

        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        if (itemCount <= 0) {
            return
        }

        detachAndScrapAttachedViews(recycler)

        //计算每个item的位置信息,存储在itemFrames里面
        for (i in 0 until itemCount) {
            //从缓存中取出
            val view = recycler.getViewForPosition(i)
            //添加到RecyclerView中
            addView(view)
            val item = micSeatLayoutParams[i]
            //测量
            measureChildWithMargins(view, item.right - item.left, item.bottom - item.top)

            Log.d(
                "MicSeatLayoutManager",
                "onLayoutChildren ${i}  ${item.left}  ${item.top}  ${item.right}  ${item.bottom}   "
            )

            layoutDecorated(view, item.left, item.top, item.right, item.bottom)
        }
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }
}



