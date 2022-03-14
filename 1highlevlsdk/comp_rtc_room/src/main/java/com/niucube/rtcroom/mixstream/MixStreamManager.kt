package com.niucube.rtcroom.mixstream

import com.qiniu.droid.rtc.QNRenderMode
import com.qiniu.droid.rtc.QNTranscodingLiveStreamingImage


interface MixStreamManager {

    /**
     * 启动前台转推 默认实现推本地轨道
     */
    fun startForwardJob()

    /**
     * 停止前台推流
     */
    fun stopForwardJob()

    /**
     * 开始混流转推
     */
    fun startMixStreamJob()
    fun stopMixStreamJob()

    /**
     * 自动混房间所有音频
     */
    fun startAutoMixAllAudio()

    fun setMixParams(params: MixStreamParams)

    //主动跟新某个座位的混流参数
    fun updateUserVideoMergeOptions(uid: String, option: MergeTrackOption?)
    fun updateUserAudioMergeOptions(uid: String, isNeed: Boolean)

    fun updateVideoMergeOptions(trackID: String, option: MergeTrackOption?)
    fun updateAudioMergeOptions(trackID: String, isNeed: Boolean)

    fun updateUserScreenMergeOptions(uid: String, option: MergeTrackOption?)
    fun updateUserCustomVideoMergeOptions(
        extraTrackTag: String,
        uid: String,
        option: MergeTrackOption?
    )

    fun updateUserCustomAudioMergeOptions(extraTrackTag: String, uid: String, isNeed: Boolean)

    class MergeTrackOption {
        var mX = 0
        var mY = 0
        var mZ = 0
        var mWidth = 0
        var mHeight = 0
        var mStretchMode: QNRenderMode? = null
    }

    class MixStreamParams {

        var mixStreamWidth: Int = 0
        var mixStringHeight: Int = 0
        var mixBitrate: Int = 3420 * 1000
        var fps: Int = 15
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

}
