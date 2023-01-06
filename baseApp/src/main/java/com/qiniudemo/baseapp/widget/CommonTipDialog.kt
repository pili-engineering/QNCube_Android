package com.qiniudemo.baseapp.widget

import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.hapi.baseframe.dialog.BaseVmBindingDialogFragment
import com.qiniu.baseapp.R
import com.qiniu.baseapp.databinding.DialogCommonTipBinding
import com.qiniu.baseapp.databinding.DialogInputDarkBinding
import java.lang.reflect.Field


/**
 * 通用提示框　待ui实现替换
 */
class CommonTipDialog {

    class TipBuild {
        var tittle = ""
        var content = ""
        var positiveText = ""
        var negativeText = "取消"
        var isNeedCancelBtn = true
        var dialogListener: FinalDialogFragment.BaseDialogListener? = null

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
            val d = CommonTipDialogImp.newInstance(this)
            dialogListener?.apply { d.setDefaultListener(this) }
            return d
        }


        fun buildDark(): FinalDialogFragment {
            val d = CommonTipDialogImpDark.newInstance(this)
            dialogListener?.apply { d.setDefaultListener(this) }
            return d
        }

        fun buildNiuHappy(): FinalDialogFragment {
            val d = CommonTipDialogImpNiuHappy.newInstance(this)
            dialogListener?.apply { d.setDefaultListener(this) }
            return d
        }

        fun buildNiuCry(): FinalDialogFragment {
            val d = CommonTipDialogImpNiuCry.newInstance(this)
            dialogListener?.apply { d.setDefaultListener(this) }
            return d
        }

        fun buildNiuNiu(): FinalDialogFragment {
            val d = CommonTipDialogImpNiuNiu.newInstance(this)
            dialogListener?.apply { d.setDefaultListener(this) }
            return d
        }
    }

}


open class CommonTipDialogImp : BaseVmBindingDialogFragment<DialogCommonTipBinding>() {

    init {
        applyCancelable(false)
        applyGravityStyle(Gravity.CENTER)
    }


    companion object {
        fun newInstance(
            tipBuild: CommonTipDialog.TipBuild
        ): CommonTipDialogImp {
            val b = Bundle()
            b.putString("title", tipBuild.tittle)
            b.putString("content", tipBuild.content)
            b.putBoolean("isNeedCancelBtn", tipBuild.isNeedCancelBtn)
            b.putString("positiveText", tipBuild.positiveText)
            b.putString("negativeText", tipBuild.negativeText)
            val f = CommonTipDialogImp()
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

    override fun initViewData() {
        arguments?.apply {
            val title = getString("title")
            if (TextUtils.isEmpty(title)) {
                binding.tvTitle.visibility = View.GONE
            } else {
                binding.tvTitle.text = title
                binding.tvTitle.visibility = View.VISIBLE
            }
            val content = getString("content")
            binding.secretPwd.text = Html.fromHtml(content)
            val isNeedCancelBtn = getBoolean("isNeedCancelBtn", true)
            if (!isNeedCancelBtn) {
                binding.vV.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
            }
            val positiveText = getString("positiveText")

            if (positiveText?.isNotEmpty() == true) {
                binding.btnConfirm.text = positiveText
            }
            val negativeText = getString("negativeText")
            if (negativeText?.isNotEmpty() == true) {
                binding.btnCancel.text = negativeText
            }
        }
        binding.btnCancel.setOnClickListener {
            mDefaultListener?.onDialogNegativeClick(this, Any())
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            dismiss()
            mDefaultListener?.onDialogPositiveClick(this, Any())
        }
    }

    override fun observeLiveData() {
    }

    override fun showLoading(toShow: Boolean) {
    }
}

class CommonTipDialogImpDark : CommonTipDialogImp() {
    companion object {
        fun newInstance(
            tipBuild: CommonTipDialog.TipBuild
        ): CommonTipDialogImpDark {
            val b = Bundle()
            b.putString("title", tipBuild.tittle)
            b.putString("content", tipBuild.content)
            b.putBoolean("isNeedCancelBtn", tipBuild.isNeedCancelBtn)
            b.putString("positiveText", tipBuild.positiveText)
            b.putString("negativeText", tipBuild.negativeText)
            val f = CommonTipDialogImpDark()
            f.arguments = b
            return f
        }
    }
}


class CommonTipDialogImpNiuHappy : CommonTipDialogImp() {
    companion object {
        fun newInstance(
            tipBuild: CommonTipDialog.TipBuild
        ): CommonTipDialogImpNiuHappy {
            val b = Bundle()
            b.putString("title", tipBuild.tittle)
            b.putString("content", tipBuild.content)
            b.putBoolean("isNeedCancelBtn", tipBuild.isNeedCancelBtn)
            b.putString("positiveText", tipBuild.positiveText)
            b.putString("negativeText", tipBuild.negativeText)
            val f = CommonTipDialogImpNiuHappy()
            f.arguments = b
            return f
        }
    }

}

class CommonTipDialogImpNiuCry : CommonTipDialogImp() {
    companion object {
        fun newInstance(
            tipBuild: CommonTipDialog.TipBuild
        ): CommonTipDialogImpNiuCry {
            val b = Bundle()
            b.putString("title", tipBuild.tittle)
            b.putString("content", tipBuild.content)
            b.putBoolean("isNeedCancelBtn", tipBuild.isNeedCancelBtn)
            b.putString("positiveText", tipBuild.positiveText)
            b.putString("negativeText", tipBuild.negativeText)
            val f = CommonTipDialogImpNiuCry()
            f.arguments = b
            return f
        }
    }
}

class CommonTipDialogImpNiuNiu : CommonTipDialogImp() {
    companion object {
        fun newInstance(
            tipBuild: CommonTipDialog.TipBuild
        ): CommonTipDialogImpNiuNiu {
            val b = Bundle()
            b.putString("title", tipBuild.tittle)
            b.putString("content", tipBuild.content)
            b.putBoolean("isNeedCancelBtn", tipBuild.isNeedCancelBtn)
            b.putString("positiveText", tipBuild.positiveText)
            b.putString("negativeText", tipBuild.negativeText)
            val f = CommonTipDialogImpNiuNiu()
            f.arguments = b
            return f
        }
    }

}


class CommonInputDialogDark : BaseVmBindingDialogFragment<DialogInputDarkBinding>() {

    companion object {
        fun newInstance(
            hint: String,
            tittle: String
        ): CommonInputDialogDark {
            val b = Bundle()
            b.putString("title", tittle)
            b.putString("hint", hint)
            val f = CommonInputDialogDark()
            f.arguments = b
            return f
        }
    }

    override fun observeLiveData() {}
    var hint: String = ""
    var title: String = ""

    override fun initViewData() {
        arguments?.let {
            hint = it.getString("hint", "")
            title = it.getString("title", "")
        }
        binding.editInput.setHint(hint)
        binding.tvTitle.text = title

        binding.btnConfirm.setOnClickListener {
            val edit = binding.editInput.text.toString()
            if (edit.isNotEmpty()) {
                mDefaultListener?.onDialogPositiveClick(this, edit)
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun showLoading(toShow: Boolean) {}
}