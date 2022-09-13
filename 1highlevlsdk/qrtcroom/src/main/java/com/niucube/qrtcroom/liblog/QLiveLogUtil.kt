package com.niucube.qrtcroom.liblog

import android.util.Log

object QLiveLogUtil {

    var isLogAble = true
    private val tag = "QNLiveRoom"

    fun d(tag:String,msg: String) {
        if (isLogAble) {
            Log.d(tag, msg)
        }
    }
    fun d(msg: String) {
        if (isLogAble) {
            Log.e(tag, msg)
        }
    }
}