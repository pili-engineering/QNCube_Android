package com.qiniu.jsonutil

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.util.ParameterizedTypeImpl
import java.lang.Exception
import java.lang.reflect.Type

object JsonUtils {

    fun <T> parseObject(text: String?, clazz: ParameterizedTypeImpl): T? {

        if (TextUtils.isEmpty(text)) {
            return null
        }
        var t: T? = null
        try {
            t = JSON.parseObject(text, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return t
    }


    fun <T> parseObject(text: String?, clazz: Class<T>): T? {
        if (TextUtils.isEmpty(text)) {
            return null
        }
        var t: T? = null
        try {
            t = JSON.parseObject(text, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return t
    }

    fun <T> parseObjectType(text: String?, clazz: Type): T? {
        if (TextUtils.isEmpty(text)) {
            return null
        }
        var t: T? = null
        try {
            t = JSON.parseObject(text, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return t
    }

    fun toJson(any: Any): String {
        return JSON.toJSONString(any)
    }
}