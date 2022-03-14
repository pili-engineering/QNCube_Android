package com.niucube.absroom

class VideoTrackParams {
    var width = 480
    var height = 640
    var fps = 15
    var bitrate = 1000
    var isMaster = true
}

class AudioTrackParams {
    var  communicationModeOn = false
}

//视频轨道参数
class ScreenTrackParams {
    var width = 720
    var height = 1080
    var fps = 15
    var tag = "screen"
        private set
    var bitrate = 1.5 * 1000
    var isMaster = false
}


