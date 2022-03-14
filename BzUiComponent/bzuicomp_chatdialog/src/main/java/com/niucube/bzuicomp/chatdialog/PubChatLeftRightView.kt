package com.niucube.bzuicomp.chatdialog

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.niucube.bzuicomp.chatdialog.PubChatLeftRightView.ChatMode.Companion.left
import com.niucube.bzuicomp.chatdialog.PubChatLeftRightView.ChatMode.Companion.right
import com.niucube.bzuicomp.chatdialog.PubChatLeftRightView.ChatMode.Companion.center
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.qiniu.bzuicomp.bottominput.IInputView
import com.qiniu.bzuicomp.bottominput.RoomInputView
import com.qiniu.bzuicomp.pubchat.*
import com.qiniusdk.userinfoprovide.UserInfoProvider
import kotlinx.android.synthetic.main.item_pubdialog_left.view.*
import kotlinx.android.synthetic.main.view_pub_chat.view.*


open class PubChatLeftRightView : FrameLayout {


    private val mInputMsgReceiver = InputMsgReceiver()
    open val mAdapter: BaseMultiItemQuickAdapter<PubChatLeftRightView.ChatMode, BaseViewHolder> = PubChatDialogItemAdapter()

    private val msgRecycler: RecyclerView?
        get() {
            return pubchatView
        }

    private var mIChatMsgCall = PubChatMsgManager.IChatMsgCall {
        if (it is IChatMsg) {
            mAdapter.addData(ChatMode(it))
            msgRecycler?.smoothScrollToPosition(mAdapter.data.size - 1)
        }
    }
    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomClosed(roomEntity: RoomEntity?) {
            super.onRoomClosed(roomEntity)
            mInputMsgReceiver.onDestroy()
            PubChatMsgManager.iChatMsgCalls.remove(mIChatMsgCall)
        }
    }

    private lateinit var tvShowInput:IInputView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.view_pub_chat, this, false)
        addView(view)
        tvShowInput = getIInputView()
        val lp = pubchatView.layoutParams
        if(isShowMsgFromBottom()){
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }else{
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        pubchatView.layoutParams=lp
        chatContainer.addView(tvShowInput.getView())
        init()
    }

    open fun isShowMsgFromBottom():Boolean{
        return true
    }
    open fun getIInputView():IInputView{
        return RoomInputView(context)
    }

    fun attachListener() {
        PubChatMsgManager.iChatMsgCalls.add(mIChatMsgCall)
        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
    }

    fun detachListener() {
        PubChatMsgManager.iChatMsgCalls.remove(mIChatMsgCall)
        RoomManager.removeRoomLifecycleMonitor(mRoomLifecycleMonitor)
    }

    fun setInputAutoChangeHeight(inputAutoChangeHeight: Boolean) {
        tvShowInput.setInputAutoChangeHeight(inputAutoChangeHeight)
    }

    fun requestEditFocus() {
        tvShowInput.requestEditFocus()
    }

    private fun init() {
        pubchatView.layoutManager = LinearLayoutManager(context)
        pubchatView.adapter = mAdapter
        tvShowInput.sendPubCall = {
            mInputMsgReceiver.buildMsg(it)
        }
    }

    fun attachActivity(activity: Activity) {
        tvShowInput.attachActivity(activity)
    }

    class ChatMode(val msg: IChatMsg) : MultiItemEntity {

        companion object {
            val left = 1
            val right = 2
            val center = 3
        }

        override fun getItemType(): Int {
            if (msg is PubChatWelCome || msg is PubChatQuitRoom) {
                return center
            }
            if (msg is PubChatMsgModel) {
                if (msg.senderId == UserInfoProvider.getLoginUserIdCall.invoke()) {
                    return right
                } else {
                    return left
                }
            }
            return left
        }
    }

    class PubChatDialogItemAdapter :
        BaseMultiItemQuickAdapter<ChatMode, BaseViewHolder>(ArrayList<ChatMode>()) {

        init {
            addItemType(left, R.layout.item_pubdialog_left);
            addItemType(right, R.layout.item_pubdialog_right);
            addItemType(center, R.layout.item_pubdialog_welcome);
        }

        override fun convert(helper: BaseViewHolder, item: ChatMode) {
            // helper.itemView.tvSenderName.text = item.mPubChatMsgModel.senderName
            if (item.itemType != center) {
                Glide.with(mContext)
                    .load((item.msg as PubChatMsgModel).sendAvatar)
                    .into(helper.itemView.ivAvatar)
                helper.itemView.tvMsgContent.text = item.msg.msgContent
            } else {
                if (item.msg is PubChatWelCome) {
                    helper.itemView.tvMsgContent.text = item.msg.msgContent
                }
                if (item.msg is PubChatQuitRoom) {
                    helper.itemView.tvMsgContent.text = item.msg.msgContent
                }
            }
        }
    }
}