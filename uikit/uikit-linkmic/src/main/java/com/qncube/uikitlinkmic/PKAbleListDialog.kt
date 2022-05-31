package com.qncube.uikitlinkmic

import android.graphics.Color
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import com.qncube.liveroomcore.datasource.RoomDataSource
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.uikitcore.dialog.FinalDialogFragment
import com.qncube.uikitcore.ext.ViewUtil
import com.qncube.uikitcore.ext.bg
import com.qncube.uikitcore.refresh.CommonEmptyView
import com.qncube.uikitcore.view.SimpleDividerDecoration
import kotlinx.android.synthetic.main.kit_dialog_pklist.*

class PKAbleListDialog() : FinalDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    private val mRoomDataSource = RoomDataSource()
    private val mAdapter = PKAnchorListAdapter()


    fun setInviteCall(inviteCall: (room: QNLiveRoomInfo) -> Unit) {
        mAdapter.inviteCall = inviteCall
    }

    override fun getViewLayoutId(): Int {
        return R.layout.kit_dialog_pklist
    }

    private fun load(page: Int) {
        bg {
            doWork {
                val data = mRoomDataSource.listRoom(page + 1, 20)
                mSmartRecyclerView.onFetchDataFinish(data.list, true)
            }
            catchError {
                mSmartRecyclerView.onFetchDataError()
            }
        }
    }

    override fun init() {
        mSmartRecyclerView.recyclerView.addItemDecoration(
            SimpleDividerDecoration(
                requireContext(),
                Color.parseColor("#EAEAEA"), ViewUtil.dip2px(1f)
            )
        )
        mSmartRecyclerView.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mSmartRecyclerView.setUp(mAdapter, CommonEmptyView(requireContext()), 3, true, true) {
            load(it)
        }
        mSmartRecyclerView.startRefresh()

    }
}

