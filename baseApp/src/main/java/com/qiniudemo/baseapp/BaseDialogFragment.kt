package com.qiniudemo.baseapp

import com.hapi.happy_dialog.BaseVmDialogFragment
import com.qiniudemo.baseapp.widget.LoadingDialog

abstract class BaseDialogFragment : BaseVmDialogFragment(){

    override fun observeLiveData() {}

    override fun showLoading(toShow: Boolean) {
        if(toShow){
            LoadingDialog.showLoading(childFragmentManager)
        }else{
            LoadingDialog.cancelLoadingDialog()
        }
    }
}