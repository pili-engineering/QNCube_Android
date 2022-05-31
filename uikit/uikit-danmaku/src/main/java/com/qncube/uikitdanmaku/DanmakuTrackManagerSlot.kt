package com.qncube.uikitdanmaku

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.qncube.danmakuservice.QNDanmakuService
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitcore.ext.ViewUtil

class DanmakuTrackManagerSlot : QNInternalViewSlot() {

    /**
     * 单个弹幕样式适配 不设置使用默认实现
     */
    var mDanmukeViewSlot: QNDanmukeViewSlot? = null

    /**
     * 没有设置代理 时候 使用的默认创建ui
     *
     * @param client
     * @param container
     * @return
     */
    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = DanmakuTrackManagerViewFrame()
        mDanmukeViewSlot?.let {
            view.mDanmukeViewSlot = it
        }
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class DanmakuTrackManagerViewFrame : BaseSlotView() {

    private val mTrackManager = TrackManager()
    private val mQNDanmakuServiceListener =
        QNDanmakuService.QNDanmakuServiceListener { model ->
            mTrackManager.onNewTrackArrive(model)
        }
    var mDanmukeViewSlot: QNDanmukeViewSlot = object : QNDanmukeViewSlot {
        override fun createView(
            lifecycleOwner: LifecycleOwner,
            context: KitContext,
            client: QNLiveRoomClient,
            container: ViewGroup?
        ): IDanmakuView {
            return DanmuTrackView(context.androidContext)
        }

        override fun getIDanmakuViewCount(): Int {
            return 3
        }

        override fun topMargin(): Int {
            return ViewUtil.dip2px(20f)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_danmaku_track_manager
    }

    override fun initView() {
        super.initView()
        for (i in 0 until mDanmukeViewSlot.getIDanmakuViewCount()) {
            val itemView = mDanmukeViewSlot.createView(lifecycleOwner!!, kitContext!!, client!!,   (  view as ViewGroup )!!)
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = mDanmukeViewSlot.topMargin()
            (  this.view as ViewGroup ).addView(itemView.getView(), lp)
            mTrackManager.addTrackView(itemView)
        }

    }
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.getService(QNDanmakuService::class.java)
                ?.removeDanmakuServiceListener(mQNDanmakuServiceListener)
        }
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        mTrackManager.onRoomLeft()
    }

    override fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {
        super.attach(lifecycleOwner, context, client)
        client.getService(QNDanmakuService::class.java)
            .addDanmakuServiceListener(mQNDanmakuServiceListener)
    }

}

interface QNDanmukeViewSlot {

    /**
     * 创建单个弹幕轨道
     * @param container
     * @return
     */
    fun createView(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): IDanmakuView


    /**
     * 弹幕轨道个数
     * @return
     */
    fun getIDanmakuViewCount(): Int

    /**
     * 距离上一个轨道的上间距
     * @return
     */
    fun topMargin(): Int
}

