package com.qiniudemo.baseapp

import com.hapi.base_mvvm.mvvm.BaseVmFragment
import com.qiniudemo.baseapp.widget.LoadingDialog

abstract class BaseFragment : BaseVmFragment() {

    override fun observeLiveData() {
    }

    override fun showLoading(toShow: Boolean) {
        if(toShow){
            LoadingDialog.showLoading(childFragmentManager)
        }else{
            LoadingDialog.cancelLoadingDialog()
        }
    }
}