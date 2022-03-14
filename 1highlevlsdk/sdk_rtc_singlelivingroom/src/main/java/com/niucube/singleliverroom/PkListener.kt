package com.niucube.singleliverroom

interface PkListener {
    //pk开启
    fun onPKStart(pkSession:PkSession)
    fun onError(code:Int ,msg:String)
    fun onPKStop(code:Int,msg:String )
    fun onPkEvent(eventKey:String,value:String)
}