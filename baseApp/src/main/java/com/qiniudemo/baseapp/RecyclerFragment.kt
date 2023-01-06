package com.qiniudemo.baseapp

import android.view.View
import com.hapi.baseframe.fragment.BaseRecyclerFragment
import com.qiniudemo.baseapp.widget.LoadingDialog

abstract class RecyclerFragment<T> : BaseRecyclerFragment<T>() {

    open fun getRefreshTempView(): View? {
        return null
    }

    override fun showLoading(toShow: Boolean) {
        if (toShow) {
            LoadingDialog.showLoading(childFragmentManager)
        } else {
            LoadingDialog.cancelLoadingDialog()
        }
    }
}