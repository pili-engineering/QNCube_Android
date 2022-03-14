package com.niucube.rtm

import android.text.TextUtils
import org.json.JSONObject

fun String.optAction():String{
    var action= ""
    try {
        val jsonObj = JSONObject(this)
         action = jsonObj.optString("action")
    }catch (e:Exception){
        e.printStackTrace()
    }
    return action?:""
}
fun String.optData():String{
    var data=""
    try {
        val jsonObj = JSONObject(this)
         data = jsonObj.optString("data")?:""
        if(TextUtils.isEmpty(data)){
            data =  jsonObj.optString("msgStr")?:""
        }
    }catch (e:java.lang.Exception){
        e.printStackTrace()
    }
    return data?:""
}

