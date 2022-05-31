package com.qncube.uikituser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.qncube.liveroomcore.ClientType
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.liveroomcore.Scheduler
import com.qncube.uikitcore.ext.toHtml
import kotlinx.android.synthetic.main.kit_view_room_timer.view.*
import java.text.DecimalFormat

class RoomTimerSlot : QNInternalViewSlot() {

    /**
     * @param time 秒
     * 时间格式化回调 默认"mm:ss"
     * 返回 格式化后html样式
     */
    var showTimeCall: ((time: Int) -> String)? = null

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View? {
        if (client.clientType == ClientType.CLIENT_PULL) {
            return null
        }
        val view = RoomTimerView()
        showTimeCall?.let {
            view.showTimeCall = it
        }
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class RoomTimerView : BaseSlotView() {

    var showTimeCall: ((time: Int) -> String) = {
        "<font color='#ffffff'>${formatTime(it)}</font>"
    }

    private fun formatTime(time: Int): String {

        val decimalFormat = DecimalFormat("00")
        val hh: String = decimalFormat.format(time / 3600)
        val mm: String = decimalFormat.format(time % 3600 / 60)
        val ss: String = decimalFormat.format(time % 60)
        return if (hh == "00") {
            "$mm:$ss"
        } else {
            "$hh:$mm:$ss"
        }
    }

    private var total = 0;
    private val mScheduler = Scheduler(1000) {
        total++
        view!!.tvTimer.text = showTimeCall(total).toHtml()
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_room_timer
    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        mScheduler.start()
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        mScheduler.cancel()
    }

}
