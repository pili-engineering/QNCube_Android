package com.niucube.channelattributes

//频道属性改变回调
interface RoomAttributesListener {
    fun onAttributesChange(roomId:String ,key:String,values:String)
    fun onAttributesClear(roomId:String ,key:String){}
}

//麦位属性回调
interface MicSeatAttributesListener {
    fun onAttributesChange(roomId:String,uid:String,key:String,values:String)
    fun onAttributesClear(roomId:String,uid:String,key:String){}
}

//频道属性操作回调
interface AttributesCallBack<T> {
    fun onSuccess(data:T)
    fun onFailure(errorCode:Int,msg:String)
}