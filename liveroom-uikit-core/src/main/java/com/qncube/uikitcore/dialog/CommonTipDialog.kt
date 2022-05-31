package com.qncube.uikitcore.dialog

import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.qncube.uikitcore.R
import kotlinx.android.synthetic.main.dialog_common_tip.*
import java.lang.reflect.Field


class CommonTipDialog : FinalDialogFragment() {

    init {
        applyCancelable(false)
        applyGravityStyle(Gravity.CENTER)
    }

    companion object {
        fun newInstance(
            tipBuild: CommonTipDialog.TipBuild
        ): CommonTipDialog {
            val b = Bundle()
            b.putString("title", tipBuild.tittle)
            b.putString("content", tipBuild.content)
            b.putBoolean("isNeedCancelBtn", tipBuild.isNeedCancelBtn)
            b.putString("positiveText", tipBuild.positiveText)
            b.putString("negativeText", tipBuild.negativeText)
            val f = CommonTipDialog()
            f.arguments = b
            return f
        }
    }


    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val mDismissed: Field = this.javaClass.superclass!!.getDeclaredField("mDismissed")
            val mShownByMe: Field = this.javaClass.superclass!!.getDeclaredField("mShownByMe")
            mDismissed.setAccessible(true)
            mShownByMe.setAccessible(true)
            mDismissed.setBoolean(this, false)
            mShownByMe.setBoolean(this, true)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val ft: FragmentTransaction = manager.beginTransaction()
        ft.add(this, javaClass.simpleName)
        ft.commitAllowingStateLoss()
    }

    override fun getViewLayoutId(): Int {
        return R.layout.dialog_common_tip
    }

    override fun init() {
        arguments?.apply {
            val title = getString("title")
            if (TextUtils.isEmpty(title)) {
                tvTitle.visibility = View.GONE
            } else {
                tvTitle.text = title
            }
            val content = getString("content")
            secret_pwd.text = Html.fromHtml(content)
            val isNeedCancelBtn = getBoolean("isNeedCancelBtn", true)
            if (!isNeedCancelBtn) {
                vV.visibility = View.GONE
                btnCancel.visibility = View.GONE
            }
            val positiveText = getString("positiveText")

            if (positiveText?.isNotEmpty() == true) {
                btnConfirm.text = positiveText
            }
            val negativeText = getString("negativeText")
            if (negativeText?.isNotEmpty() == true) {
                btnCancel.text = negativeText
            }
        }
        btnCancel.setOnClickListener {
            mDefaultListener?.onDialogNegativeClick(this, Any())
            dismiss()
        }

        btnConfirm.setOnClickListener {
            dismiss()
            mDefaultListener?.onDialogPositiveClick(this, Any())
        }
    }

    public class TipBuild {
        var tittle = ""
            private set
        var content = ""
            private set
        var positiveText = ""
            private set
        var negativeText = "取消"
            private set
        var isNeedCancelBtn = true
            private set
        var dialogListener: FinalDialogFragment.BaseDialogListener? = null
            private set

        fun setTittle(tittle: String): TipBuild {
            this.tittle = tittle
            return this
        }

        fun setContent(content: String): TipBuild {
            this.content = content
            return this
        }

        fun setPositiveText(confirm: String): TipBuild {
            positiveText = confirm
            return this
        }

        fun isNeedCancelBtn(isNeedCancelBtn: Boolean): TipBuild {

            this.isNeedCancelBtn = isNeedCancelBtn
            return this
        }

        fun setNegativeText(negativeText: String): TipBuild {
            this.negativeText = negativeText
            return this
        }

        fun setListener(listener: FinalDialogFragment.BaseDialogListener): TipBuild {
            dialogListener = listener
            return this
        }

        fun build(): FinalDialogFragment {
            val d = CommonTipDialog.newInstance(this)
            dialogListener?.apply { d.setDefaultListener(this) }
            return d
        }
    }
}
