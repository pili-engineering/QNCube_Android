package com.qncube.liveroomcore

import android.util.Log

object QNLiveLogUtil {

    var isLogAble = true
    private val tag = "QNLiveRoom"

    fun logD(msg: String) {
        if (isLogAble) {
            Log.d(tag, msg)
        }
    }

    fun LogE(msg: String) {
        if (isLogAble) {
            Log.e(tag, msg)
        }
    }

}