package com.qiniu.bzuicomp.pubchat

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.Html
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.bzui_item_pub_chat.view.*


class PubChatAdapter  : BaseQuickAdapter<IChatMsg, BaseViewHolder>(R.layout.bzui_item_pub_chat,ArrayList<IChatMsg>()){

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun convert(holder: BaseViewHolder, item: IChatMsg) {

        holder.itemView.tvText.text = Html.fromHtml(item.pubchat_getMsgHtml(),
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