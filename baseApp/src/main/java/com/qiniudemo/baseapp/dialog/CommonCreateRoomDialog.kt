package com.qiniudemo.baseapp.dialog

import android.text.TextUtils
import android.view.Gravity
import com.qiniu.baseapp.R
import com.qiniudemo.baseapp.BaseDialogFragment
import com.qiniudemo.baseapp.ext.asToast
import kotlinx.android.synthetic.main.dialog_common_create_room.*

class CommonCreateRoomDialog : BaseDialogFragment() {


    init {
        applyGravityStyle(Gravity.CENTER)
    }

    override fun initViewData() {
        btnCancel.setOnClickListener {
            dismiss()
        }
        btnConfirm.setOnClickListener {
            val text = etRoomTittle.text
            if(TextUtils.isEmpty(text)){
                "请输入房间标题".asToast()
                return@setOnClickListener
            }
            mDefaultListener?.onDialogPositiveClick(this,text)
            dismiss()
        }
    }

    override fun getViewLayoutId(): Int {
       return R.layout.dialog_common_create_room
    }
}