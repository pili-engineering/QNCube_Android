package com.qiniu.bzuicomp.pubchat

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.Html
import com.hapi.baseframe.adapter.QRecyclerViewBindAdapter
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.qiniu.bzuicomp.pubchat.databinding.BzuiItemPubChatBinding

class PubChatAdapter  :QRecyclerViewBindAdapter<IChatMsg, BzuiItemPubChatBinding>(){

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun convertViewBindHolder(
        helper: QRecyclerViewBindHolder<BzuiItemPubChatBinding>,
        item: IChatMsg
    ) {
        helper.binding.tvText.text = Html.fromHtml(item.pubchat_getMsgHtml(),
           Html.ImageGetter {source->
               val id: Int = source.toInt()
               val drawable: Drawable = mContext.resources.getDrawable(id, null)
               drawable.setBounds(
                   0, 0,
                  ((drawable.intrinsicWidth *0.3).toInt()),
                  ((drawable.intrinsicHeight *0.3).toInt()));
               drawable
           }, null);
    }

}