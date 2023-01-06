package com.niucube.bzuicomp.chatdialog

import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.ViewGroup
import com.hapi.baseframe.dialog.FinalDialogFragment

class PubChatDialog(private val activityContext: Context) : FinalDialogFragment() {

    private val mPubChatLeftRightView = PubChatLeftRightView(activityContext)

    init {
        mPubChatLeftRightView.attachListener()
        applyGravityStyle(Gravity.BOTTOM)
        applyCancelable(true)
        applyDimAmount(0f)
    }

    override fun getViewLayoutId(): Int {
        return R.layout.dialog_pub_chat
    }

    override fun init() {
        view?.findViewById<ViewGroup>(R.id.flChatFragment)?.addView(mPubChatLeftRightView)
        mPubChatLeftRightView.attachActivity(requireActivity())
        mPubChatLeftRightView.requestEditFocus()
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
}