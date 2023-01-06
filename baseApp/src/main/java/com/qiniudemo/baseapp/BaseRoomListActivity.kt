package com.qiniudemo.baseapp

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.bumptech.glide.Glide
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.adapter.QRecyclerViewHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hipi.vm.backGround
import com.qiniu.baseapp.R
import com.qiniu.baseapp.databinding.ItemRoomListBinding
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.been.RoomListItem
import com.qiniudemo.baseapp.service.RoomService

abstract class BaseRoomListActivity : RecyclerActivity<RoomListItem>() {

    @Autowired
    @JvmField
    var solutionType = ""

    abstract var defaultType: String

    override fun checkDefaultAutowired() {
        if (solutionType == "") {
            solutionType = defaultType
        }
    }

    override val layoutManager: RecyclerView.LayoutManager
            by lazy { GridLayoutManager(this, 2) }

    override val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            showLoading(true)
            doWork {
                val pageData = RetrofitManager.create(RoomService::class.java)
                    .listRoom(10, it + 1, solutionType)
                mSmartRecycler.onFetchDataFinish(pageData.list, true)
            }
            catchError {
                it.printStackTrace()
                mSmartRecycler.onFetchDataError()
            }
            onFinally {
                showLoading(false)
            }
        }
    }

    override fun isRefreshAtOnStart(): Boolean {
        return false
    }

    override fun isRefreshAtOnResume(): Boolean {
        return true
    }

    open class BaseRoomItemAdapter : QSmartViewBindAdapter<RoomListItem, ItemRoomListBinding>() {
        /**
         * 如果player上面需要加布局　用这个
         */
        open fun getCoverLayout(parent: ViewGroup): View? {
            return null
        }

        override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): QRecyclerViewHolder? {
            val vh = super.onCreateDefViewHolder(parent, viewType)
            val cl = getCoverLayout(parent)
            if (cl != null) {
                vh?.itemView?.findViewById<ViewGroup>(R.id.flItemContent)?.addView(cl)
            }
            return vh
        }

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemRoomListBinding>,
            item: RoomListItem
        ) {
            helper.binding.tvRoomName.text = item.title
            Glide.with(mContext)
                .load(item.image)
                .into(helper.binding.ivRoomItemBg)
            helper.binding.tvRoomMemb.text = item.totalUsers
        }
    }
}