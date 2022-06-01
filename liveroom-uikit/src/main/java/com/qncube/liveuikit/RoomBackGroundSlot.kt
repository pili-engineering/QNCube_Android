package com.qncube.liveuikit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import kotlinx.android.synthetic.main.kit_img_bg.view.*

/**
 * 房间背景图
 */
class RoomBackGroundSlot : QNInternalViewSlot() {

    //默认背景图片
    var defaultBackGroundImg = R.drawable.kit_dafault_room_bg

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = RoomBackGroundView();
        view.defaultBackGroundImg = defaultBackGroundImg
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class RoomBackGroundView : BaseSlotView() {

    var defaultBackGroundImg = R.drawable.kit_dafault_room_bg
    override fun getLayoutId(): Int {
        return R.layout.kit_img_bg
    }

    override fun initView() {
        super.initView()
        view!!.ivBg.setImageResource(defaultBackGroundImg)
    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        Glide.with(context!!).load(roomInfo.coverUrl)
            .into(view!!.ivBg)
    }
}