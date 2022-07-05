package com.qiniudemo.baseapp

import android.view.View
import com.hapi.base_mvvm.mvvm.BaseRecyclerFragment
import com.hapi.base_mvvm.refresh.IEmptyView
import com.qiniudemo.baseapp.widget.CommonEmptyView
import com.qiniudemo.baseapp.widget.LoadingDialog

abstract class RecyclerFragment<T> : BaseRecyclerFragment<T>() {

    /**
     * 通用emptyView
     */
    /**
     * 通用emptyView
     */
    override fun getEmptyView(): IEmptyView? {

        return CommonEmptyView(requireContext()).apply {
            val temp = getRefreshTempView()
            if (temp != null) {
                setRefreshingView(temp)
                setStatus(IEmptyView.START_REFREASH_WHEN_EMPTY)
            }
        }
    }

    open fun getRefreshTempView(): View? {
        return null
    }

    override fun observeLiveData() {}

    override fun showLoading(toShow: Boolean) {
        if (toShow) {
            LoadingDialog.showLoading(childFragmentManager)
        } else {
            LoadingDialog.cancelLoadingDialog()
        }
    }
}