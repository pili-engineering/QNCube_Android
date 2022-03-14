package com.qiniudemo.baseapp.widget


import android.view.Gravity

import androidx.fragment.app.FragmentManager

import com.hapi.happy_dialog.FinalDialogFragment
import com.qiniu.baseapp.R


object LoadingDialog {

    private var isShow = false
    private val loadingDialog by lazy {
        MyLoading()

    }

    class MyLoading : FinalDialogFragment() {

        init {
            applyGravityStyle(Gravity.CENTER)
        }

        override fun onStart() {

            super.onStart()
            dialog?.window?.setDimAmount(0f)
        }

        override fun getViewLayoutId(): Int {
            return R.layout.dialog_loading

        }

        override fun init() {

        }

        override fun dismiss() {
            try {
                super.dismiss()
            }catch (e:IllegalStateException){
                e.printStackTrace()
            }
            isShow = false
        }
    }

    fun showLoading(fm: FragmentManager) {
        if (!isShow) {
            loadingDialog.show(fm, "")
            isShow = true
        }
    }

    fun cancelLoadingDialog() {
        if(isShow){
            loadingDialog.dismiss()
            isShow = false
        }
    }

}