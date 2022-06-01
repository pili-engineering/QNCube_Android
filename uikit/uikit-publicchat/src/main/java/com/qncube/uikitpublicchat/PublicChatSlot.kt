package com.qncube.uikitpublicchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.publicchatservice.PubChatModel
import com.qncube.publicchatservice.QNPublicChatService
import com.qncube.uikitcore.*
import kotlinx.android.synthetic.main.kit_view_publicchatslotview.view.*

class PublicChatSlot : QNInternalViewSlot() {

    /**
     * 如何消息每个消息文本 适配
     */
    var mPubChatMsgShowAdapter: QNPubChatMsgShowAdapter? = null

    /**
     * 自定义每个消息布局
     */
    var mViewAdapterSlot: QNViewAdapterSlot<PubChatModel>? = null


    /**
     * 点击事件回调
     */
    var mClickCallback: QNViewClickSlot<PubChatModel>? = null

    var mRoomNoticeHeader = RoomNoticeSlot()

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
        val view = PublicChatSlotView()
        mPubChatMsgShowAdapter?.let {
            view.mPubChatMsgShowAdapter = it
        }
        mViewAdapterSlot?.let {
            view.mViewAdapterSlot = it
        }
        mRoomNoticeHeader.let {
            view.mRoomNoticeHeader = it
        }
        mClickCallback?.let {
            view.mClickCallback = it
        }
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}


/**
 * 消息显示适配器
 */
interface QNPubChatMsgShowAdapter {
    /**
     * 根据消息返回显示样式
     * @param mode
     * @return
     */
    fun showHtml(mode: PubChatModel): String
}

class PublicChatSlotView : BaseSlotView() {

    var mRoomNoticeHeader: RoomNoticeSlot? = null

    var mPubChatMsgShowAdapter: QNPubChatMsgShowAdapter = object : QNPubChatMsgShowAdapter {
        override fun showHtml(mode: PubChatModel): String {
            return "<font color='#ffffff'>${mode.content}</font>"
        }
    }
    var mViewAdapterSlot: QNViewAdapterSlot<PubChatModel> =
        QNViewAdapterSlot<PubChatModel> { _, _, _ ->
            val ad = PubChatAdapter()
            ad.mPubChatMsgShowAdapter = mPubChatMsgShowAdapter
            ad
        }
    var mClickCallback: QNViewClickSlot<PubChatModel>? = null

    private lateinit var mAdapter: BaseQuickAdapter<PubChatModel, BaseViewHolder>

    private var hasHeader = false
    private val mPublicChatServiceLister = QNPublicChatService.QNPublicChatServiceLister {
        if (it.senderRoomId != roomInfo?.liveId) {
            return@QNPublicChatServiceLister
        }
        mAdapter.addData(it)
        val position = if (hasHeader) {
            mAdapter.data.size
        } else {
            mAdapter.data.size - 1
        }
        view!!.recyChat.smoothScrollToPosition(position)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.getService(QNPublicChatService::class.java)
                ?.removePublicChatServiceLister(mPublicChatServiceLister)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_publicchatslotview
    }

    override fun initView() {
        super.initView()
        view!!.recyChat.layoutManager = LinearLayoutManager(context)
        view!!.recyChat.adapter = mAdapter
        mRoomNoticeHeader?.createView(lifecycleOwner!!, kitContext!!, client!!, view as ViewGroup)
            ?.let {
                hasHeader = true
                mAdapter.addHeaderView(it)
            }
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
    }

    override fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {

        super.attach(lifecycleOwner, context, client)
        mAdapter = mViewAdapterSlot.createAdapter(lifecycleOwner, context, client)
        if (mAdapter is PubChatAdapter) {
            (mAdapter as PubChatAdapter).mHeadClickCall = { i, v ->
                mClickCallback?.createItemClick(context, client, i)?.onClick(v)
            }
        }
        client.getService(QNPublicChatService::class.java)
            .addPublicChatServiceLister(mPublicChatServiceLister)
    }

}