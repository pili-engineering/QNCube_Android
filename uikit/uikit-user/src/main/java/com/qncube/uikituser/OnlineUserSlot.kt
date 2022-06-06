package com.qncube.uikituser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qncube.chatservice.QNChatRoomService
import com.qncube.chatservice.QNChatRoomServiceListener
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.Scheduler
import com.qncube.liveroomcore.mode.QNLiveUser
import com.qncube.liveroomcore.datasource.UserDataSource
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.uikitcore.*
import com.qncube.uikitcore.ext.bg
import kotlinx.android.synthetic.main.kit_view_online.view.*

class OnlineUserSlot : QNInternalViewSlot() {


    /**
     * 内置槽位 设置每个用户item自定义布局
     */
    var mViewAdapterSlot: QNViewAdapterSlot<QNLiveUser>? = null

    /**
     * 内置槽位 设置 点击事件回调
     */
    var mClickCallback: QNViewClickSlot<QNLiveUser>? = null

    /**
     * 没有设置代理 时候 使用的默认创建ui
     *
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
        val view = OnlineUserView()
        mViewAdapterSlot?.let {
            view.mViewAdapterSlot = it
        }
        mClickCallback?.let {
            view.mClickCallback = it
        }
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class OnlineUserView : BaseSlotView() {

    /**
     * 内置槽位 设置每个用户item自定义布局
     */
    var mViewAdapterSlot: QNViewAdapterSlot<QNLiveUser>? = null
    private val mUserDataSource by lazy { UserDataSource() }

    /**
     * 内置槽位 设置 点击事件回调
     */
    var mClickCallback: QNViewClickSlot<QNLiveUser>? = null
    var adapter: BaseQuickAdapter<QNLiveUser, BaseViewHolder> = OnlineUserViewAdapter()
    private val mChatRoomServiceListener = object : QNChatRoomServiceListener {
        override fun onUserJoin(memberId: String) {
            refresh()
        }

        override fun onUserLevel(memberId: String) {
            refresh()
        }

        override fun onReceivedC2CMsg(msg: String, fromId: String, toId: String) {}
        override fun onReceivedGroupMsg(msg: String, fromId: String, toId: String) {}
        override fun onUserBeKicked(memberId: String) {}
        override fun onUserBeMuted(isMute: Boolean, memberId: String, duration: Long) {}
        override fun onAdminAdd(memberId: String) {}
        override fun onAdminRemoved(memberId: String, reason: String) {}
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_online
    }

    override fun initView() {
        super.initView()
        view?.recyOnline?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter.setOnItemClickListener { _, view, position ->
            mClickCallback?.createItemClick(kitContext!!, client!!, adapter.data.get(position))
                ?.onClick(view)
        }
        mViewAdapterSlot?.let {
            adapter = it.createAdapter(lifecycleOwner!!, kitContext!!, client!!)
        }
        view!!.recyOnline.adapter = adapter
    }

    private var roomId = ""

    private val lazyFreshJob = Scheduler(30000) {
        refresh()
    }

    private fun refresh() {
        if (roomId.isEmpty()) {
            return
        }
        lifecycleOwner?.bg {
            doWork {
                val users = mUserDataSource.getOnlineUser(roomId, 1, 10)
                adapter.setNewData(users.list ?: ArrayList<QNLiveUser>())
            }
            catchError {
            }
        }
    }

    override fun onRoomEnter(roomId: String, user: QNLiveUser) {
        super.onRoomEnter(roomId, user)
        this.roomId = roomId

    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        lazyFreshJob.start()
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        roomId = ""
        adapter.setNewData(ArrayList<QNLiveUser>())
        lazyFreshJob.cancel()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.getService(QNChatRoomService::class.java)
                ?.removeChatServiceListener(mChatRoomServiceListener)
        }
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

