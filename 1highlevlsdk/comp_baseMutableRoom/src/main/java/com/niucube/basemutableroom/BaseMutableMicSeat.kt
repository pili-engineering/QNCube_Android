package com.niucube.basemutableroom
import com.niucube.basemutableroom.absroom.seat.UserMicSeat


//基础多人麦位置
open class BaseMutableMicSeat : UserMicSeat() {
    var isMuteVideoByMe = false // 视频是不是被我屏蔽了
    var isMuteAudioByMe = false //音频是不是被我屏蔽了
    override fun isOpenAudio():Boolean{
        return isOwnerOpenAudio && !isMuteAudioByMe
    }

    override fun isOpenVideo():Boolean{
        return isOwnerOpenVideo && !isMuteVideoByMe
    }
}