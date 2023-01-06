package com.qiniudemo.baseapp.dialog

import android.text.TextUtils
import android.view.Gravity
import com.qiniu.baseapp.databinding.DialogCommonCreateRoomBinding
import com.qiniudemo.baseapp.BaseDialogFragment
import com.qiniudemo.baseapp.ext.asToast

class CommonCreateRoomDialog : BaseDialogFragment<DialogCommonCreateRoomBinding>() {

    init {
        applyGravityStyle(Gravity.CENTER)
    }

    override fun initViewData() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnConfirm.setOnClickListener {
            val text = binding.etRoomTittle.text
            if (TextUtils.isEmpty(text)) {
                "请输入房间标题".asToast()
                return@setOnClickListener
            }
            mDefaultListener?.onDialogPositiveClick(this, text)
            dismiss()
        }
    }

}