package com.qncube.uikitlinkmic

import androidx.recyclerview.widget.LinearLayoutManager
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.uikitcore.dialog.FinalDialogFragment
import com.qncube.uikitcore.ext.bg
import com.qncube.uikitcore.refresh.CommonEmptyView
import kotlinx.android.synthetic.main.kit_dialog_pklist.*

class PKAbleListDialog() : FinalDialogFragment() {

    var inviteCall: (room: QNLiveRoomInfo) -> Unit = {
    }
    private val mAdapter = PKAnchorListAdapter().apply {
        inviteCall = inviteCall
    }

    override fun getViewLayoutId(): Int {
        return R.layout.kit_dialog_pklist
    }

    private fun load(page: Int) {
        bg {
            doWork {
                mSmartRecyclerView.onFetchDataFinish(ArrayList<QNLiveRoomInfo>(), true)
            }
            catchError {
                mSmartRecyclerView.onFetchDataError()
            }
        }
    }

    override fun init() {
        mSmartRecyclerView.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mSmartRecyclerView.setUp(mAdapter, CommonEmptyView(requireContext()), 3, true, true) {
            load(it)
        }
    }
}

