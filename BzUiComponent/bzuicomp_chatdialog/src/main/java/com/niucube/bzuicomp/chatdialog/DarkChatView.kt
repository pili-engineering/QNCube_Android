package com.niucube.bzuicomp.chatdialog

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import com.qiniu.bzuicomp.bottominput.DarkInputView
import com.qiniu.bzuicomp.bottominput.IInputView


class DarkChatView : PubChatLeftRightView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun isShowMsgFromBottom(): Boolean {
        return false
    }
    override fun getIInputView(): IInputView {
        return DarkInputView(context)
    }
}