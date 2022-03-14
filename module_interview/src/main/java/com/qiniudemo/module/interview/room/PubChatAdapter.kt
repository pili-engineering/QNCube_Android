package com.qiniudemo.module.interview.room

import android.annotation.SuppressLint
import android.text.Html
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qiniu.bzuicomp.pubchat.IChatMsg
import com.qiniu.bzuicomp.pubchat.PubChatMsgModel
import com.qiniu.bzuicomp.pubchat.PubChatQuitRoom
import com.qiniu.bzuicomp.pubchat.PubChatWelCome
import com.qiniudemo.module.interview.R
import kotlinx.android.synthetic.main.interview_item_chat.view.*


class PubChatAdapter : BaseQuickAdapter<IChatMsg, BaseViewHolder>(
    R.layout.interview_item_chat,
    ArrayList<IChatMsg>()
) {

    @SuppressLint("SetTextI18n")
    override fun convert(helper: BaseViewHolder, item: IChatMsg) {

        when (item) {
            is PubChatMsgModel -> {
                helper.itemView.tvChatLine.text = "${item.senderName}:${item.msgContent}"
            }
            is PubChatWelCome -> {
                helper.itemView.tvChatLine.text = "${item.senderName} ${item.msgContent}"
            }
            is PubChatQuitRoom -> {
                helper.itemView.tvChatLine.text = "${item.senderName} ${item.msgContent}"
            }
            else -> {
                helper.itemView.tvChatLine.text = Html.fromHtml(item.getMsgHtml())
            }
        }
    }
}