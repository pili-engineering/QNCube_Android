package com.qiniu.bzuicomp.pubchat

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.baseframe.adapter.QRecyclerAdapter
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.qiniu.bzuicomp.pubchat.databinding.ViewBzuiPubchatBinding

/**
 * 公屏
 */
class CommonChatView : FrameLayout, LifecycleObserver {

    private var adapter: QRecyclerAdapter<IChatMsg> = PubChatAdapter()
    private lateinit var binding: ViewBzuiPubchatBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = ViewBzuiPubchatBinding.inflate(LayoutInflater.from(context), this, true)
        binding.chatRecy.layoutManager = LinearLayoutManager(context)
    }

    fun setAdapter(chatAdapter: QRecyclerAdapter<IChatMsg> = PubChatAdapter()) {
        adapter = chatAdapter
        binding.chatRecy.adapter = adapter
    }

    private val roomMonitor = object : RoomLifecycleMonitor {
        @SuppressLint("NotifyDataSetChanged")
        override fun onRoomLeft(roomEntity: RoomEntity?) {
            adapter.data.clear()
            adapter.notifyDataSetChanged()
        }
    }

    private var mIChatMsgCall = PubChatMsgManager.IChatMsgCall {
        adapter.addData(it)
        binding.chatRecy.smoothScrollToPosition(adapter.data.size - 1)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        //   RtmChannelKit.removeRtmChannelListener(channelListener)
        RoomManager.removeRoomLifecycleMonitor(roomMonitor)
        PubChatMsgManager.iChatMsgCalls.remove(mIChatMsgCall)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        //    RtmChannelKit.addRtmChannelListener(channelListener)
        RoomManager.addRoomLifecycleMonitor(roomMonitor)
        PubChatMsgManager.iChatMsgCalls.add(mIChatMsgCall)
    }
}