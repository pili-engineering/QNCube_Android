package com.niucube.lazysitmutableroom

import com.niucube.absroom.BaseMutableMicSeat

class LazySitUserMicSeat : BaseMutableMicSeat() {

    var isForbiddenAudioByManager = false //管理员关麦克风
    var isForbiddenVideoByManager = false //管理员关摄像头

    fun clear() {
        uid = ""
        isOwnerOpenAudio = false
        isOwnerOpenVideo = false
        isMuteVideoByMe = false
        isMuteAudioByMe = false
        userExtension = null
        isForbiddenAudioByManager = false
        isForbiddenVideoByManager = false
    }

    override fun isOpenAudio():Boolean{
        return isOwnerOpenAudio && !isMuteAudioByMe && !isForbiddenAudioByManager
    }

    override fun isOpenVideo():Boolean{
        return isOwnerOpenVideo && !isMuteVideoByMe && !isForbiddenVideoByManager
    }

}