package com.qiniudemo.baseapp

import androidx.viewbinding.ViewBinding
import com.hapi.baseframe.fragment.BaseBindingFragment
import com.qiniudemo.baseapp.widget.LoadingDialog

abstract class BaseFragment<T : ViewBinding> : BaseBindingFragment<T>() {

    override fun showLoading(toShow: Boolean) {
        if (toShow) {
            LoadingDialog.showLoading(childFragmentManager)
        } else {
            LoadingDialog.cancelLoadingDialog()
        }
    }
}