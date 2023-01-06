package com.qiniudemo.baseapp

import androidx.viewbinding.ViewBinding
import com.hapi.baseframe.dialog.BaseVmBindingDialogFragment
import com.qiniudemo.baseapp.widget.LoadingDialog

abstract class BaseDialogFragment<T : ViewBinding> : BaseVmBindingDialogFragment<T>() {

    override fun showLoading(toShow: Boolean) {
        if (toShow) {
            LoadingDialog.showLoading(childFragmentManager)
        } else {
            LoadingDialog.cancelLoadingDialog()
        }
    }
}