package com.qncube.uikituser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitcore.ext.toHtml
import kotlinx.android.synthetic.main.kit_view_room_id.view.*

class RoomIdSlot : QNInternalViewSlot() {

    //文本回调 默认显示房间ID
    var getShowTextCall: ((roomInfo: QNLiveRoomInfo) -> String)? = null

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = RoomIdView()
        getShowTextCall?.let {
            view.getShowTextCall = it
        }
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class RoomIdView : BaseSlotView() {

    var getShowTextCall: ((roomInfo: QNLiveRoomInfo) -> String) = { info ->
        "<font color='#ffffff'>${info.liveId}</font>"
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_room_id
    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        view!!.tvRoomId.text = getShowTextCall.invoke(roomInfo).toHtml()
    }

}

