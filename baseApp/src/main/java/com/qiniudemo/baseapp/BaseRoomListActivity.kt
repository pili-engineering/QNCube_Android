package com.qiniudemo.baseapp

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hipi.vm.backGround
import com.qiniu.baseapp.R
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.been.RoomListItem
import com.qiniudemo.baseapp.service.RoomService
import kotlinx.android.synthetic.main.item_room_list.view.*

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
                    .listRoom(10, it+1, solutionType)
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

    override fun initViewData() {
        super.initViewData()
    }

    override fun isRefreshAtOnStart(): Boolean {
        return false
    }

    override fun isRefreshAtOnResume(): Boolean {
        return true
    }

    open class BaseRoomItemAdapter:
        BaseQuickAdapter<com.qiniudemo.baseapp.been.RoomListItem, BaseViewHolder>(R.layout.item_room_list, ArrayList<com.qiniudemo.baseapp.been.RoomListItem>()) {

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
            }
            return vh
        }

        override fun convert(helper: BaseViewHolder, item: com.qiniudemo.baseapp.been.RoomListItem) {
            helper.itemView.tvRoomName.text = item.title
            Glide.with(mContext)
                .load(item.image)
                .into(helper.itemView.ivRoomItemBg)
            helper.itemView.tvRoomMemb.text = item.totalUsers
        }
    }
}