package com.qizhou.bzupdate.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.hapi.ut.SpUtil
import com.qiniudemo.baseapp.BaseDialogFragment
import com.qizhou.bzupdate.UpDataModel
import com.qizhou.bzupdate.databinding.DialogfragUpdataBinding

class UpdateDialog : BaseDialogFragment<DialogfragUpdataBinding>() {

    private var upDataModel: UpDataModel? = null
    private var enforcement = false


    init {
        applyCancelable(false)
        applyGravityStyle(Gravity.CENTER)
    }

    companion object {
        fun newInstance(upDataModel: UpDataModel, enforcement: Boolean): UpdateDialog {
            val d = UpdateDialog()
            val b = Bundle()
            b.putParcelable("upDataModel", upDataModel)
            b.putBoolean("enforcement", enforcement)
            d.arguments = b
            return d
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initViewData() {
        arguments?.apply {
            upDataModel = getParcelable("upDataModel")
            enforcement = getBoolean("enforcement")
        }

        binding.ivClose.visibility = if (enforcement) View.GONE else View.VISIBLE
        binding.ivClose.setOnClickListener {
            // 用户点击了稍后再更新，那么以后都不能弹出更新对话框，除非是强制更新，或者点击了设置的更新功能
            SpUtil.get("update").saveData("isShow", false)
            dismiss()
        }
        binding.tvVersion.text = "新版本：${upDataModel?.version}"

        binding.tvUpDataContent.text = upDataModel?.msg
        binding.btnOk.setOnClickListener {
            mDefaultListener?.onDialogPositiveClick(this, Unit)
            dismiss()
        }
    }


}