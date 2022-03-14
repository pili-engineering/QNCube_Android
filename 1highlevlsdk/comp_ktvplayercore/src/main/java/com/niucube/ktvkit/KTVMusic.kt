package com.niucube.ktvkit

//歌曲信令信息
class KTVMusic<T> {

    companion object {
        val playStatus_pause = 0
        val playStatus_playing = 1
        val playStatus_error = 2
        val playStatus_completed = 3
    }

    //音乐ID
    var musicId :String =""
    //混音主人ID
    var mixerUid = ""
    //开始播放的时间戳
    var startTimeMillis: Long = 0
    //当前进度对应的时间戳
    var currentTimeMillis: Long = 0
    //当前播放进度
    var currentPosition: Long = 0
    //播放状态 0 暂停  1 播放  2 出错
    var playStatus: Int = 1
    //音乐总长度
    var duration: Long = 0
    //音轨名称
    var trackType=""
    //播放的歌曲信息
    var musicInfo: T? = null
}