package com.qiniu.droid.audio2text.doracore

import android.text.TextUtils
import com.google.gson.Gson


object JsonUtils {



    fun <T> parseObjectOrigin(text: String?, clazz: Class<T>): T? {
        if (TextUtils.isEmpty(text)) {
            return null
        }
        var t: T? = null
        try {
            t = Gson().fromJson(text,clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return t
    }

}