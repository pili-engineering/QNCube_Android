package com.qncube.uikitcore.dialog


import android.view.Gravity

import androidx.fragment.app.FragmentManager
import com.qncube.uikitcore.R

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
            return R.layout.kit_dialog_loading
        }

        override fun init() {
        }

        override fun dismiss() {
            try {
                super.dismiss()
            } catch (e: IllegalStateException) {
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
        if (isShow) {
            loadingDialog.dismiss()
            isShow = false
        }
    }

}