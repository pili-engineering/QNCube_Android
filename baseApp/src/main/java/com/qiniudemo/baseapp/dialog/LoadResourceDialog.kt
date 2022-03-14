package com.qiniudemo.baseapp.dialog

import android.view.Gravity
import com.qiniu.baseapp.R
import com.qiniudemo.baseapp.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_load_resources.*

class LoadResourceDialog : BaseDialogFragment() {

    init {
        applyGravityStyle(Gravity.CENTER)
        applyDimAmount(0f)
        applyCancelable(false)
    }

    override fun initViewData() {}

    fun onProgress(progress: Float, currentFileName: String) {
        progressbar.setCurrentProgress(progress)
        progressbar.setTipText(currentFileName.substring(currentFileName.lastIndexOf("/") + 1))
    }

    override fun getViewLayoutId(): Int {
        return R.layout.dialog_load_resources
    }
}