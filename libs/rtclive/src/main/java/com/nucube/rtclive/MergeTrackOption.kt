package com.nucube.rtclive

import com.qiniu.droid.rtc.QNRenderMode
import com.qiniu.droid.rtc.QNTranscodingLiveStreamingImage

/**
 * 混流画布参数
 */
class MixStreamParams {
    var mixStreamWidth: Int = 0
    var mixStringHeight: Int = 0
    var mixBitrate: Int = 3420 * 1000
    var fps: Int = 25
    var qnBackGround: QNTranscodingLiveStreamingImage? = null
    var watermarks: List<QNTranscodingLiveStreamingImage>? = null

    constructor(
        mixStreamWidth: Int = 0,
        mixStringHeight: Int = 0,
        mixBitrate: Int = 3420 * 1000,
        fps: Int = 15,
        qnBackGround: QNTranscodingLiveStreamingImage? = null
    ) {
        this.mixStreamWidth = mixStreamWidth
        this.mixStringHeight = mixStringHeight
        this.mixBitrate = mixBitrate
        this.fps = fps
        this.qnBackGround = qnBackGround
    }

    constructor()
}


interface TrackMergeOption {

}
class CameraMergeOption : TrackMergeOption {
    var isNeed: Boolean = false
    var mX = 0
    var mY = 0
    var mZ = 0
    var mWidth = 0
    var mHeight = 0
    var mStretchMode: QNRenderMode? = null
}

class MicrophoneMergeOption : TrackMergeOption {
    var isNeed: Boolean = false
}

class QNMergeOption {
    var uid: String = ""
    var cameraMergeOption: CameraMergeOption = CameraMergeOption()
    var microphoneMergeOption: MicrophoneMergeOption = MicrophoneMergeOption()

}