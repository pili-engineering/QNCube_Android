package com.niucube.bzuicomp.chatdialog

import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.ViewGroup
import com.hapi.baseframe.dialog.BaseVmBindingDialogFragment
import com.niucube.bzuicomp.chatdialog.databinding.DialogPubChatBinding

class LightPubChatDialog(val activityContext: Context) :
    BaseVmBindingDialogFragment<DialogPubChatBinding>() {


    private val mPubChatLeftRightView = LightChatView(activityContext)
    private var backGroundRes = -1

    init {
        mPubChatLeftRightView.attachListener()
        applyGravityStyle(Gravity.BOTTOM)
        applyCancelable(true)
        applyDimAmount(0f)
    }

    override fun init() {
        if (backGroundRes > 0) {
            binding.flChatFragment.setBackgroundResource(backGroundRes)
        }
        binding.flChatFragment.addView(mPubChatLeftRightView)
        mPubChatLeftRightView.attachActivity(requireActivity())
        mPubChatLeftRightView.requestEditFocus()
    }

    fun setBackGround(resId: Int) {
        backGroundRes = resId
    }

    override fun onDestroyView() {
        if (mPubChatLeftRightView.parent != null) {
            (mPubChatLeftRightView.parent as ViewGroup).removeAllViews()
        }
        super.onDestroyView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun showLoading(toShow: Boolean) {

    }

    override fun initViewData() {

    }
}