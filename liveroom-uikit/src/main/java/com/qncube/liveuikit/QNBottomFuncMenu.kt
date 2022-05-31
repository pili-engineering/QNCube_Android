package com.qncube.liveuikit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import com.qncube.liveroomcore.ClientType
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.asToast
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitcore.dialog.LoadingDialog
import com.qncube.uikitdanmaku.SendDanmakuView
import com.qncube.uikitlinkmic.StartLinkView
import com.qucube.uikitinput.RoomInputDialog
import kotlinx.android.synthetic.main.kit_bottom_bar.view.*
import kotlin.collections.ArrayList

//发弹幕菜单
class SendDanmakuFucMenu : QNInternalViewSlot() {
    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = SendDanmakuView()
        view.attach(lifecycleOwner, context, client)
        val sendView = view.createView(LayoutInflater.from(context.androidContext), container)
        sendView.setOnClickListener {
            RoomInputDialog().apply {
                sendPubCall = {
                    view.send(it)
                }
            }.show(context.fm, "")
        }
        return sendView
    }
}

//申请连麦菜单
class ApplyLinkFucMenu : QNInternalViewSlot() {
    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = StartLinkView()
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

//关闭房间菜单
class CloseRoomFucMenu : QNInternalViewSlot() {

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = LayoutInflater.from(context.androidContext)
            .inflate(R.layout.kit_close_menu_view, container, false)
        view.setOnClickListener {
            LoadingDialog.showLoading(context.fm)
            client.leaveRoom(object : QNLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    LoadingDialog.cancelLoadingDialog()
                    msg?.asToast()
                }

                override fun onSuccess(data: Void?) {
                    LoadingDialog.cancelLoadingDialog()
                    client.closeRoom()
                    context.currentActivity.finish()
                }
            })
        }
        return view
    }

}

class ShowBeautyFucMenu : QNInternalViewSlot() {
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
    ): View? {
        return null
    }
}

//底部菜单栏目
class BottomFucBarSlot : QNInternalViewSlot() {

    //主播菜单默认 发弹幕 美颜 关房间
    val mAnchorFuncMenus = ArrayList<QNInternalViewSlot>().apply {
        add(SendDanmakuFucMenu())
        add(ShowBeautyFucMenu())
        add(CloseRoomFucMenu())
    }

    //用户菜单默认 发弹幕 连麦 关房间
    val mAudienceFuncMenus = ArrayList<QNInternalViewSlot>().apply {
        add(SendDanmakuFucMenu())
        add(ApplyLinkFucMenu())
        add(CloseRoomFucMenu())
    }

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val bar = BottomFucBar()
        bar.views = if(client.clientType==ClientType.CLIENT_PUSH){
            mAnchorFuncMenus
        }else{
            mAudienceFuncMenus
        }
        bar.attach(lifecycleOwner, context, client)
        return bar.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class BottomFucBar : BaseSlotView() {

    var views: List<QNInternalViewSlot>? = null
    override fun getLayoutId(): Int {
        return R.layout.kit_bottom_bar
    }

    override fun initView() {
        super.initView()
        views?.forEach {
            it.createView(lifecycleOwner!!, kitContext!!, client!!, view!!.llContent)
                ?.let { child ->
                    view!!.llContent.addView(child)
                }
        }
    }
}