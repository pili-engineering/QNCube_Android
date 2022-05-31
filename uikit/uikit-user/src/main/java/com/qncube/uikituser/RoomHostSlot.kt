package com.qncube.uikituser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitcore.QNViewClickSlot
import com.qncube.uikitcore.ext.toHtml
import kotlinx.android.synthetic.main.kit_view_room_host_slot.view.*

class RoomHostSlot : QNInternalViewSlot() {


    //房主头像点击事件回调 提供点击事件自定义回调
    var mClickCallBack: QNViewClickSlot<QNLiveUser>? = null

    /**
     * 标题自定义显示回调 默认房间标题
     */
    var showHostTitleCall: ((room: QNLiveRoomInfo) -> String)? = null

    /**
     * 副标题自定义回调 默认房间ID
     */
    var showSubTitleCall: ((room: QNLiveRoomInfo) -> String)? = null


    /**
     * 没有设置代理 时候 使用的默认创建ui
     * @param activity
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
        val viewHostView = RoomHostView()
        mClickCallBack?.let {
            viewHostView.mClickCallBack = it
        }
        showHostTitleCall?.let {
            viewHostView.showHostTitleCall = it
        }
        showSubTitleCall?.let {
            viewHostView.showSubTitleCall = it
        }
        viewHostView.attach(lifecycleOwner, context, client)
        val view = viewHostView.createView(LayoutInflater.from(context.androidContext), container)
        return view
    }
}

class RoomHostView : BaseSlotView() {

    //房主头像点击事件回调 提供点击事件自定义回调
    var mClickCallBack: QNViewClickSlot<QNLiveUser>? = null

    /**
     * 标题自定义显示回调 默认房间标题
     */
    var showHostTitleCall: ((room: QNLiveRoomInfo) -> String) = {
        "<font color='#ffffff'>" + it.title + "</font>"
    }

    /**
     * 副标题自定义回调 默认房间ID
     */
    var showSubTitleCall: ((room: QNLiveRoomInfo) -> String) = {
        "<font color='#ffffff'>" + it.anchorInfo.nick + "</font>"
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_room_host_slot;
    }

    override fun initView() {
        super.initView()

    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        view!!.findViewById<View>(R.id.ivHost).setOnClickListener {
            mClickCallBack?.createItemClick(kitContext!!, client!!, roomInfo.anchorInfo)
        }
        view!!.findViewById<TextView>(R.id.tvTitle).text = showHostTitleCall.invoke(roomInfo).toHtml()
        view!!.findViewById<TextView>(R.id.tvSubTitle).text = showSubTitleCall.invoke(roomInfo).toHtml()
        Glide.with(context!!)
            .load(roomInfo.anchorInfo.avatar)
            .into(view!!.findViewById<ImageView>(R.id.ivHost))

    }

}

