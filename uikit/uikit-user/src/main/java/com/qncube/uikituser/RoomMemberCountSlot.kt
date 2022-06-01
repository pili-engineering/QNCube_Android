package com.qncube.uikituser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.qncube.chatservice.QNChatRoomService
import com.qncube.chatservice.QNChatRoomServiceListener
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.datasource.RoomDataSource
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitcore.QNViewClickSlot
import com.qncube.liveroomcore.Scheduler
import com.qncube.uikitcore.ext.ViewUtil
import kotlinx.android.synthetic.main.kit_view_room_member_count.view.*

/**
 * 右上角房间人数 位置
 */
class RoomMemberCountSlot : QNInternalViewSlot() {

    /**
     * 点击回调
     */
    var mClickCallBack: QNViewClickSlot<Unit>? = null

    /**
     * 没有设置代理 时候 使用的默认创建ui
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
        val view = RoomMemberCountView()
        mClickCallBack?.let {
            view.mClickCallBack = it
        }
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }

}

class RoomMemberCountView : BaseSlotView() {

    private val mRoomDaraSource = RoomDataSource()
    var mClickCallBack: QNViewClickSlot<Unit>? = null
    private val mChatRoomServiceListener = object : QNChatRoomServiceListener {
        override fun onUserJoin(memberId: String) {
            refresh(true)
        }

        override fun onUserLevel(memberId: String) {
            refresh(true)
        }

        override fun onReceivedC2CMsg(msg: String, fromId: String, toId: String) {}
        override fun onReceivedGroupMsg(msg: String, fromId: String, toId: String) {}
        override fun onUserBeKicked(memberId: String) {
            refresh(false)
        }

        override fun onUserBeMuted(isMute: Boolean, memberId: String, duration: Long) {}
        override fun onAdminAdd(memberId: String) {}
        override fun onAdminRemoved(memberId: String, reason: String) {}
    }


    private fun refresh(add: Boolean?) {

        var count = -1
        try {
            count = (view!!.tvCount.text.toString().toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (count == -1) {
            return
        }
        if (add == true) {
            count++
        }
        if (add == false) {
            count--
        }
        view!!.tvCount.text = count.toString()
        checkTextSize()
    }

    private fun checkTextSize() {
//        if (view!!.tvCount.text.length > 2) {
//            view!!.tvCount.textSize = ViewUtil.sp2px(3f).toFloat()
//        } else {
//            view!!.tvCount.textSize = ViewUtil.sp2px(14f).toFloat()
//        }
    }

    private val mScheduler = Scheduler(15000) {
        if (roomInfo == null) {
            return@Scheduler
        }
        try {
            val room = mRoomDaraSource.refreshRoomInfo(roomInfo!!.liveId)
            view!!.tvCount.text = room.onlineCount.toString()
            checkTextSize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        mScheduler.start()
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        mScheduler.cancel()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.getService(QNChatRoomService::class.java)
                ?.removeChatServiceListener(mChatRoomServiceListener)
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_room_member_count
    }

    override fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {
        super.attach(lifecycleOwner, context, client)
        client.getService(QNChatRoomService::class.java)
            .addChatServiceListener(mChatRoomServiceListener)
    }
}





