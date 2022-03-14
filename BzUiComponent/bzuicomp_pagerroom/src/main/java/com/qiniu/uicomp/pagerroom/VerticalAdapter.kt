package com.qiniu.uicomp.pagerroom;

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

abstract class VerticalAdapter<T>(res: Int) :
    BaseQuickAdapter<T, BaseViewHolder>(res, ArrayList<T>()) {

    override fun getItemCount(): Int {
        if (data.size == 1) {
            return 1
        }
        return Int.MAX_VALUE
    }

    override fun getItemViewType(position: Int): Int {
           return super.getItemViewType(position % data.size)
    }

    override fun getItem(position: Int): T? {
        return data[position % data.size]
    }
}