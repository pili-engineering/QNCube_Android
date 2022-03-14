package com.qiniudemo.baseapp.been

import com.niucube.comproom.RoomEntity
import com.qiniu.bzcomp.user.UserInfoManager

val isOwnerOpenAudio = "isOwnerOpenAudio"
val isOwnerOpenVideo = "isOwnerOpenVideo"
val isMuteVideoByMe = "isMuteVideoByMe"
val isMuteAudioByMe = "isMuteAudioByMe"


fun List<Attribute>.findValueOfKey(key: String): String {
    this.forEach {
        if (it.key == key) {
            return it.value
        }
    }
    return ""
}

fun RoomEntity.asBaseRoomEntity(): BaseRoomEntity {
    return ( this as BaseRoomEntity )
}

fun RoomEntity.isRoomHost(): Boolean {
   return ( this as BaseRoomEntity ).isRoomHost()
}
fun RoomEntity.hostId(): String {
    return ( this as BaseRoomEntity? )?.roomInfo?.creator?:""
}

fun BaseRoomEntity.isRoomHost(): Boolean {
    return roomInfo?.creator == UserInfoManager.getUserId()
}