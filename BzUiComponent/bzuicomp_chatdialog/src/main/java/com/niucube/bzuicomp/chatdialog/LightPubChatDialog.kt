package com.niucube.bzuicomp.chatdialog

import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.ViewGroup
import com.hapi.happy_dialog.FinalDialogFragment
import kotlinx.android.synthetic.main.dialog_pub_chat.*

class LightPubChatDialog (val activityContext: Context) : FinalDialogFragment() {


    private val mPubChatLeftRightView = LightChatView(activityContext)
    private var backGroundRes = -1
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
        if(backGroundRes>0){
            flChatFragment.setBackgroundResource(backGroundRes)
        }
        flChatFragment.addView(mPubChatLeftRightView)
        mPubChatLeftRightView.attachActivity(requireActivity())
        mPubChatLeftRightView.requestEditFocus()
    }

    fun setBackGround(resId:Int){
        backGroundRes = resId
        if(flChatFragment!=null){
            flChatFragment.setBackgroundResource(resId)
        }
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