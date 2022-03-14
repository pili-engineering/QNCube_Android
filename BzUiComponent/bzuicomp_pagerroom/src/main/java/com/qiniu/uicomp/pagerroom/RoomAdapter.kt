package com.qiniu.uicomp.pagerroom;

import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_room.view.*
import java.util.logging.Logger

/**
 * 房间上下滑动切换适配器
 */
open class RoomAdapter<T : ShowCoverAble> : VerticalAdapter<T>(R.layout.item_room) {

    /**
     * 如果player上面需要加布局　用这个
     */
    open fun getCoverLayout(parent: ViewGroup): View? {
        return null
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder? {
        val vh = super.onCreateDefViewHolder(parent, viewType)
        val cl = getCoverLayout(parent)
        if (cl != null) {
            vh.itemView.flItemContent.addView(cl)
            vh.itemView.flItemContent.visibility = View.VISIBLE
        }
        return vh
    }

    override fun convert(helper: BaseViewHolder, item: T) {
        Glide.with(mContext).load(item.providePageCover())
            .into(helper.itemView.ivRoomCover)

    }
}