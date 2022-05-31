package com.qncube.uikitlinkmic

import android.view.Gravity
import com.qncube.uikitcore.dialog.FinalDialogFragment
import kotlinx.android.synthetic.main.kit_dialog_apply.*

class LinkApplyDialog : FinalDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    override fun getViewLayoutId(): Int {
        return R.layout.kit_dialog_apply
    }

    override fun init() {
        llAudio.setOnClickListener {
            dismiss()
            mDefaultListener?.onDialogPositiveClick(this, false)
        }
        llVideo.setOnClickListener {
            dismiss()
            mDefaultListener?.onDialogPositiveClick(this, true)
        }
    }
}