package com.qiniudemo.baseapp.ext

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.util.ParameterizedTypeImpl
import com.qiniu.bzcomp.network.HttpResp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Response

fun <T> Response.toHttpData(type:Class<T>): HttpResp<T>? {
    var httpRes : HttpResp<T>?=null
    try {
        val p =
            ParameterizedTypeImpl(
                arrayOf(type),
                HttpResp::class.java,
                HttpResp::class.java
            )
         httpRes = JSON.parseObject(this?.body?.string(),p)
         if(httpRes?.code!=0){
             GlobalScope.launch(Dispatchers.Main) {
                 httpRes?.message?.asToast()
             }
         }
    }catch (e:Exception){
        e.printStackTrace()
    }
    return httpRes
}