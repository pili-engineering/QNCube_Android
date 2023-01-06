package com.qiniudemo.module.interview.room

import android.annotation.SuppressLint
import android.text.Html
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.qiniu.bzuicomp.pubchat.IChatMsg
import com.qiniu.bzuicomp.pubchat.PubChatMsgModel
import com.qiniu.bzuicomp.pubchat.PubChatQuitRoom
import com.qiniu.bzuicomp.pubchat.PubChatWelCome
import com.qiniudemo.module.interview.databinding.InterviewItemChatBinding

class PubChatAdapter : QSmartViewBindAdapter<IChatMsg, InterviewItemChatBinding>() {

    @SuppressLint("SetTextI18n")
    override fun convertViewBindHolder(
        helper: QRecyclerViewBindHolder<InterviewItemChatBinding>,
        item: IChatMsg
    ) {
        when (item) {
            is PubChatMsgModel -> {
                helper.binding.tvChatLine.text = "${item.senderName}:${item.msgContent}"
            }
            is PubChatWelCome -> {
                helper.binding.tvChatLine.text = "${item.senderName} ${item.msgContent}"
            }
            is PubChatQuitRoom -> {
                helper.binding.tvChatLine.text = "${item.senderName} ${item.msgContent}"
            }
            else -> {
                helper.binding.tvChatLine.text = Html.fromHtml(item.pubchat_getMsgHtml())
            }
        }
    }
}