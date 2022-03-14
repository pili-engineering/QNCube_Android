package com.qiniu.bzuicomp.bottominput

import android.app.Activity
import android.view.View

interface IInputView {

    fun setInputAutoChangeHeight(inputAutoChangeHeight: Boolean)
    fun requestEditFocus()
    var sendPubCall: ((msg: String) -> Unit)?
    fun attachActivity(activity: Activity)
    fun getView(): View

}