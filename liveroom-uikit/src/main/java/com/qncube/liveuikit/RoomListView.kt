package com.qncube.liveuikit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.datasource.RoomDataSource
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.asToast
import com.qncube.uikitcore.dialog.LoadingDialog
import com.qncube.uikitcore.ext.ViewUtil
import com.qncube.uikitcore.ext.bg
import com.qncube.uikitcore.refresh.CommonEmptyView
import kotlinx.android.synthetic.main.kit_roomlist_item_room.view.*
import kotlinx.android.synthetic.main.kit_view_room_list.view.*

class RoomListView : FrameLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.kit_view_room_list, this, true)
        mSmartRecyclerView.recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    private val mAdapter = RoomListAdapter()
    private var mLifecycleOwner: LifecycleOwner? = null

    fun attach(lifecycleOwner: LifecycleOwner) {
        mLifecycleOwner = lifecycleOwner

        mSmartRecyclerView.setUp(mAdapter, CommonEmptyView(context), 10, true, true) {
            load(it)
        }
        mAdapter.goJoinCall = {
            // LoadingDialog.showLoading(fragmentManager)
            QNLiveRoomUIKit.joinRoom(context, it.liveId, object : QNLiveCallBack<QNLiveRoomInfo> {
                override fun onError(code: Int, msg: String?) {
                    msg?.asToast()
                }

                override fun onSuccess(data: QNLiveRoomInfo?) {
                    //   LoadingDialog.cancelLoadingDialog()
                }
            })
        }
        mLifecycleOwner?.lifecycleScope?.launchWhenResumed {
            mSmartRecyclerView.startRefresh()
        }
    }

    private fun load(page: Int) {
        mLifecycleOwner?.bg {
            doWork {
                val data = RoomDataSource().listRoom(page + 1, 20)
                mSmartRecyclerView.onFetchDataFinish(data.list, true)
            }
            catchError {
                mSmartRecyclerView.onFetchDataError()
            }
        }
    }

    class RoomListAdapter : BaseQuickAdapter<QNLiveRoomInfo, BaseViewHolder>(
        R.layout.kit_roomlist_item_room,
        ArrayList()
    ) {

        var goJoinCall: (item: QNLiveRoomInfo) -> Unit = {}
        override fun convert(helper: BaseViewHolder, item: QNLiveRoomInfo) {
            helper.itemView.setOnClickListener {
                goJoinCall.invoke(item)
            }
            Glide.with(mContext).load(item.coverUrl)
                .apply(RequestOptions().transform(RoundedCorners(ViewUtil.dip2px(8f))))
                .into(helper.itemView.ivCover)
            helper.itemView.tvRoomId.text = item.anchorInfo.nick
            helper.itemView.tvRoomName.text = item.title
            helper.itemView.tvOnlineCount.text = item.onlineCount.toString()
        }
    }
}