package com.qncube.liveroomcore

enum class LiveStatus(var tipMsg: String) {
    LiveStatusPrepare(""),
    LiveStatusOn("ss"),
    LiveStatusAnchorOnline("ss"),
    LiveStatusAnchorOffline("主播已离线"),
    LiveStatusOff("房间已关闭")
}

fun Int.roomStatusToLiveStatus(): LiveStatus {
    return when (this) {
        0 -> LiveStatus.LiveStatusPrepare
        1 -> LiveStatus.LiveStatusOn
        else -> LiveStatus.LiveStatusOff
    }
}

fun Int.anchorStatusToLiveStatus(): LiveStatus {
    return when (this) {
        1 -> LiveStatus.LiveStatusAnchorOnline
        0 -> LiveStatus.LiveStatusAnchorOffline
        else -> LiveStatus.LiveStatusAnchorOffline
    }
}