package com.qncube.liveroomcore

enum class LiveStatus(val intValue: Int, var tipMsg: String) {

    LiveStatusPrepare(0, ""),
    LiveStatusOn(1, "ss"),
    LiveStatusAnchorOnline(2, "ss"),
    LiveStatusAnchorOffline(3, "主播已离线"),
    LiveStatusOff(4, "房间已关闭")

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
        0 -> LiveStatus.LiveStatusAnchorOnline
        1 -> LiveStatus.LiveStatusAnchorOffline
        else -> LiveStatus.LiveStatusAnchorOffline
    }
}