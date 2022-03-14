package com.niucube.module.videowatch.mode

class MovieSignal {
    //电影ID
    var videoId :String =""
    //电影控制者ID
    var videoUid = ""
    //开始播放的时间戳
    var startTimeMillis: Long = 0
    //当前进度对应的时间戳
    var currentTimeMillis: Long = 0
    //当前播放进度
    var currentPosition: Long = 0
    //播放状态 0 暂停 1 播放 2 出错
    var playStatus: Int = 1

    //当前电影实体 （服务器端定义的结构）
    var movieInfo: Movie? = null
}