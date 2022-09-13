package com.niucube.qrtcroom.qplayer

import com.pili.pldroid.player.AVOptions

/**
 *  播放器配置
 */
object QMediaPlayerConfig {
    /**
     * 播放器配置
     */
    var mAVOptionsGetter :()-> AVOptions={
        AVOptions().apply {
            setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
            setInteger(AVOptions.KEY_FAST_OPEN, 1);
            setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 5);
            setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
            setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_SW_DECODE);
            setInteger(AVOptions.KEY_LOG_LEVEL, 5)
        }
    }
}