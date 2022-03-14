package com.qizhou.bzupdate.dialog

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.hapi.ut.SpUtil
import com.qiniudemo.baseapp.BaseDialogFragment

import com.qizhou.bzupdate.R
import com.qizhou.bzupdate.UpDataModel
import kotlinx.android.synthetic.main.dialogfrag_updata.*

class UpdateDialog : BaseDialogFragment() {

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


    /**
     * 通过onCreateView方式配置的布局layoutId
     */
    override fun getViewLayoutId(): Int {
        return R.layout.dialogfrag_updata
    }

    override fun initViewData() {
        arguments?.apply {
            upDataModel = getParcelable("upDataModel")
            enforcement = getBoolean("enforcement")
        }

        ivClose.visibility = if (enforcement) View.GONE else View.VISIBLE
        ivClose.setOnClickListener {
            // 用户点击了稍后再更新，那么以后都不能弹出更新对话框，除非是强制更新，或者点击了设置的更新功能
            SpUtil.get("update").saveData("isShow", false)
            dismiss()
        }
        tvVersion.text = "新版本：${upDataModel?.version}"

        tvUpDataContent.text = upDataModel?.msg
        btnOk.setOnClickListener {
            mDefaultListener?.onDialogPositiveClick(this, Unit)
            dismiss()
        }
    }


}