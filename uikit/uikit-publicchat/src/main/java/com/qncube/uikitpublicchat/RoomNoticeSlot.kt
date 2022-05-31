package com.qncube.uikitpublicchat

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
import kotlinx.android.synthetic.main.kit_notice_view.view.*


/**
 * 公告槽位
 */
class RoomNoticeSlot : QNInternalViewSlot() {

    /**
     * 自定义显示样式
     */
    var noticeHtmlShowAdapter: ((notice: String) -> String)? = null

    //背景
    var backgroundView: Int = -1

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = RoomNoticeView()
        if (backgroundView != -1) {
            view.backgroundView = backgroundView
        }
        noticeHtmlShowAdapter?.let {
            view.noticeHtmlShowAdapter = it
        }
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class RoomNoticeView : BaseSlotView() {

    var noticeHtmlShowAdapter: ((notice: String) -> String) = {
        "<font color='#ffffff'>${it}</font>"
    }
    //背景
    var backgroundView: Int = R.drawable.kit_shape_40000000_6

    override fun getLayoutId(): Int {
        return R.layout.kit_notice_view
    }

    override fun initView() {
        super.initView()
        view!!. tvNotice.setBackgroundResource(backgroundView)
    }
    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
       view!!. tvNotice.text = noticeHtmlShowAdapter.invoke(roomInfo.notice).toHtml()
    }


}