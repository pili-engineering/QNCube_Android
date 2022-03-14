package com.qizhou.bzupdate.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View

import com.qiniudemo.baseapp.BaseDialogFragment
import com.qizhou.bzupdate.R
import kotlinx.android.synthetic.main.dialog_updata_progress.*

class UpdataProgressDialog : BaseDialogFragment() {

    private var enforcement = false

    companion object {
        fun newInstance(enforcement: Boolean): UpdataProgressDialog {
            val d = UpdataProgressDialog()
            val b = Bundle()

            b.putBoolean("enforcement", enforcement)
            d.arguments = b
            return d
        }
    }

    init {
        applyCancelable(false)
        applyGravityStyle(Gravity.CENTER)
    }

    /**
     * 通过onCreateView方式配置的布局layoutId
     */
    override fun getViewLayoutId(): Int {
        return R.layout.dialog_updata_progress
    }

    override fun initViewData() {
        arguments?.apply {
            enforcement = getBoolean("enforcement")
        }
        if (enforcement) {
            btnBg.visibility = View.GONE
        } else {
            btnBg.visibility = View.VISIBLE
        }

        btnBg.setOnClickListener {
            dismiss()
        }
    }

     fun onComplete(isFinish: Boolean?) {
        if (!enforcement) {
            dismiss()
        }
    }

     fun onUpdateProgress(current: Long?, total: Long?) {
        if (current == null || total == null) {
            return
        }
        val ratio = (current / (total?.toDouble()))
        val process = ratio * 100
        tvProcess.text = "${process.toInt()}%"
        pbUpdateProgress.progress = process.toInt()
        tvProcess.translationX = (pbUpdateProgress.width * ratio).toFloat()
    }


}