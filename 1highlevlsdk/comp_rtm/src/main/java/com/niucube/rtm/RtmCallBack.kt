package com.niucube.rtm

/**
 * im操作回调
 */
interface RtmCallBack {
    fun onSuccess()
    fun onFailure(code:Int,msg:String)
}