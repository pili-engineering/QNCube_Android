package com.niucube.basemutableroom.absroom.seat

//用户麦位
open class UserMicSeat: MicSeat(){

    var isOwnerOpenAudio = false //麦位主人是不是打开了声音
    var isOwnerOpenVideo = false //麦位主人是不是打开了视频

    var userExtension : UserExtension?=null //麦位用户扩展字段

    open fun isOpenAudio():Boolean{
        return isOwnerOpenAudio
    }

   open fun isOpenVideo():Boolean{
        return isOwnerOpenVideo
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }
}