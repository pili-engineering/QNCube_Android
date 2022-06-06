package com.qncube.liveroomcore

enum class LiveStatus(val intValue: Int, val tipMsg: String) {

    LiveStatusPrepare(0, ""),
    LiveStatusOn(1,"ss"),
    LiveStatusOff(2,"房间已关闭")

}