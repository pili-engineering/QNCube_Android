package com.niucube.bzuicomp.chatdialog

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hapi.baseframe.adapter.*
import com.niucube.bzuicomp.chatdialog.PubChatLeftRightView.ChatMode.Companion.left
import com.niucube.bzuicomp.chatdialog.PubChatLeftRightView.ChatMode.Companion.right
import com.niucube.bzuicomp.chatdialog.PubChatLeftRightView.ChatMode.Companion.center
import com.niucube.bzuicomp.chatdialog.databinding.ItemPubdialogLeftBinding
import com.niucube.bzuicomp.chatdialog.databinding.ItemPubdialogRightBinding
import com.niucube.bzuicomp.chatdialog.databinding.ItemPubdialogWelcomeBinding
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.qiniu.bzuicomp.bottominput.IInputView
import com.qiniu.bzuicomp.bottominput.RoomInputView
import com.qiniu.bzuicomp.pubchat.*
import com.qiniusdk.userinfoprovide.UserInfoProvider

open class PubChatLeftRightView : FrameLayout {

    private val mInputMsgReceiver = InputMsgReceiver()
    open val mAdapter : QRecyclerAdapter<ChatMode> = PubChatDialogItemAdapter()

    private val msgRecycler: RecyclerView
        get() {
            return pubchatView
        }

    private var mIChatMsgCall = PubChatMsgManager.IChatMsgCall {
        if (it is IChatMsg) {
            mAdapter.addData(ChatMode(it))
            msgRecycler.smoothScrollToPosition(mAdapter.data.size - 1)
        }
    }
    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomClosed(roomEntity: RoomEntity?) {
            super.onRoomClosed(roomEntity)
            mInputMsgReceiver.onDestroy()
            PubChatMsgManager.iChatMsgCalls.remove(mIChatMsgCall)
        }
    }

    private lateinit var tvShowInput: IInputView
    private lateinit var pubchatView: RecyclerView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.view_pub_chat, this, false)
        addView(view)

        pubchatView = view.findViewById(R.id.pubChatView)
        tvShowInput = getIInputView()
        val lp = pubchatView.layoutParams
        if (isShowMsgFromBottom()) {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        pubchatView.layoutParams = lp
        view.findViewById<LinearLayout>(R.id.chatContainer).addView(tvShowInput.getView())
        init()
    }

    open fun isShowMsgFromBottom(): Boolean {
        return true
    }

    open fun getIInputView(): IInputView {
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
            return if (msg.pubchat_sendID() == UserInfoProvider.getLoginUserIdCall.invoke()) {
                right
            } else {
                left
            }
        }
    }

    class PubChatDialogItemAdapter :
        QMultipleItemRvAdapter<ChatMode>(ArrayList<ChatMode>()) {

        override fun getViewType(t: ChatMode): Int {
            return t.itemType
        }

        override fun registerItemProvider() {
            itemProvider[left] = LeftItemProvider()
            itemProvider[right] = RightItemProvider()
            itemProvider[center] = CenterItemProvider()
        }

        class LeftItemProvider() : ViewBindingItemProvider<ChatMode, ItemPubdialogLeftBinding>() {
            override fun convertViewBindHolder(
                helper: QRecyclerViewBindHolder<ItemPubdialogLeftBinding>,
                data: ChatMode,
                position: Int
            ) {
                Glide.with(helper.itemView.context)
                    .load((data.msg).pubchat_senderAvatar())
                    .into(helper.binding.ivAvatar)
                if (data.msg.pubchat_getMsgAction() == PubChatMsgModel.action_pubText) {
                    helper.binding.tvMsgContent.text = data.msg.pubchat_msgOrigin()
                } else {
                    helper.binding.tvMsgContent.text =
                        Html.fromHtml(data.msg.pubchat_getMsgHtml(), Html.ImageGetter { source ->
                            val id: Int = source.toInt()
                            val drawable: Drawable = mContext!!.resources.getDrawable(id, null)
                            drawable.setBounds(
                                0, 0,
                                ((drawable.intrinsicWidth * 0.3).toInt()),
                                ((drawable.intrinsicHeight * 0.3).toInt())
                            );
                            drawable
                        }, null)
                }
            }
        }

        class CenterItemProvider() :
            ViewBindingItemProvider<ChatMode, ItemPubdialogWelcomeBinding>() {
            override fun convertViewBindHolder(
                helper: QRecyclerViewBindHolder<ItemPubdialogWelcomeBinding>,
                data: ChatMode,
                position: Int
            ) {
                if (data.msg is PubChatWelCome) {
                    helper.binding.tvMsgContent.text = data.msg.msgContent
                }
                if (data.msg is PubChatQuitRoom) {
                    helper.binding.tvMsgContent.text = data.msg.msgContent
                }
            }
        }

        class RightItemProvider() : ViewBindingItemProvider<ChatMode, ItemPubdialogRightBinding>() {
            override fun convertViewBindHolder(
                helper: QRecyclerViewBindHolder<ItemPubdialogRightBinding>,
                data: ChatMode,
                position: Int
            ) {
                Glide.with(helper.itemView.context)
                    .load((data.msg).pubchat_senderAvatar())
                    .into(helper.binding.ivAvatar)
                if (data.msg.pubchat_getMsgAction() == PubChatMsgModel.action_pubText) {
                    helper.binding.tvMsgContent.text = data.msg.pubchat_msgOrigin()
                } else {
                    helper.binding.tvMsgContent.text =
                        Html.fromHtml(data.msg.pubchat_getMsgHtml(), Html.ImageGetter { source ->
                            val id: Int = source.toInt()
                            val drawable: Drawable = mContext!!.resources.getDrawable(id, null)
                            drawable.setBounds(
                                0, 0,
                                ((drawable.intrinsicWidth * 0.3).toInt()),
                                ((drawable.intrinsicHeight * 0.3).toInt())
                            );
                            drawable
                        }, null)
                }
            }
        }
    }
}