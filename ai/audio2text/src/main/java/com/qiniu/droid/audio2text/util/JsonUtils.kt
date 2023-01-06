package com.qiniu.droid.audio2text.util

import android.text.TextUtils
import com.google.gson.Gson
import java.lang.reflect.Type


object JsonUtils {

    private val gson = Gson()
    fun <T> parseObject(text: String?, clazz: Type): T? {
        if (TextUtils.isEmpty(text)) {
            return null
        }
        var t: T? = null
        try {
            t = gson.fromJson(text, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return t
    }

    fun <T> parseObjectWithOutTry(text: String?, clazz: Type): T? {
        if (TextUtils.isEmpty(text)) {
            return null
        }
        var t: T? = null
        t = gson.fromJson(text, clazz)
        return t
    }

    fun <T> parseObject(text: String?, clazz: Class<T>): T? {
        if (TextUtils.isEmpty(text)) {
            return null
        }
        var t: T? = null
        try {
            t = gson.fromJson(text, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return t
    }

    fun toJson(any: Any): String {
        return gson.toJson(any)
    }

}