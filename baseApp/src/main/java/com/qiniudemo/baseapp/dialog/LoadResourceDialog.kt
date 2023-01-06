package com.qiniudemo.baseapp.dialog

import android.view.Gravity
import com.qiniu.baseapp.R
import com.qiniu.baseapp.databinding.DialogLoadResourcesBinding
import com.qiniudemo.baseapp.BaseDialogFragment

class LoadResourceDialog : BaseDialogFragment<DialogLoadResourcesBinding>() {

    init {
        applyGravityStyle(Gravity.CENTER)
        applyDimAmount(0f)
        applyCancelable(false)
    }

    override fun initViewData() {}

    fun onProgress(progress: Float, currentFileName: String) {
        binding.progressbar.setCurrentProgress(progress)
        binding.progressbar.setTipText(currentFileName.substring(currentFileName.lastIndexOf("/") + 1))
    }

}