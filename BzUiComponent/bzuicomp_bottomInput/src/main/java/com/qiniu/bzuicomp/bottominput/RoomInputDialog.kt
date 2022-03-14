package com.qiniu.bzuicomp.bottominput

import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import com.hapi.happy_dialog.FinalDialogFragment
import com.hapi.ut.SoftInputUtil
import kotlinx.android.synthetic.main.dialog_room_input.*


/**
 * 底部输入框
 */
class RoomInputDialog(var type: Int = type_text) : FinalDialogFragment() {

    companion object {
        const val type_text = 1
        const val type_danmu = 2
    }

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    /**
     * 发消息拦截回调
     */
    var sendPubCall: ((msg: String) -> Unit)? = null
    override fun getViewLayoutId(): Int {
      return R.layout.dialog_room_input
    }

    override fun init() {
        viewInput.attachActivity(requireActivity())
        viewInput.sendPubCall = {
            sendPubCall?.invoke(it)
            dismiss()
        }
        viewInput.requestEditFocus()
    }
}