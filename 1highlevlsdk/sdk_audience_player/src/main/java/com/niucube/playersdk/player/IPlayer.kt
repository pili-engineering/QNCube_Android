package com.niucube.playersdk.player

import android.net.Uri

interface IPlayer {
    /**
     * 获得当前播放url
     */

    fun getCurrentUrl(): Uri?

    /**
     * 开始播放
     *
     * @param preLoading  预加载　　提前异步装载视频　　如果true 装载完成将等待　　startPlay
     */
    fun setUp(uir: Uri, headers: Map<String, String>? = null, preLoading: Boolean = false)

    /**
     * 取消preLoading　的等待　如果　preLoading　　如果装载完成将直接播放　否则还是等装载完成才播放
     */
    fun startPlay()


    fun addPlayStatusListener(lister: PlayerStatusListener, isAdd: Boolean)
    /**
     * 暂停
     */
    fun pause()

    fun stop()
    /**
     * 恢复
     */
    fun resume()

    /**
     * 播放配置
     */
    fun setPlayerConfig(playerConfig: PlayerConfig)

    fun getPlayerConfig(): PlayerConfig


    fun seekTo(pos: Int)


    /**
     * 设置音量
     *
     * @param volume 音量值
     */
    fun setVolume(volume: Int)


    fun getCurrentPlayStatus(): Int

    fun getBufferPercentage(): Int

    /**
     * 获取最大音量
     *
     * @return 最大音量值
     */
    fun getMaxVolume(): Int

    /**
     * 获取当前音量
     *
     * @return 当前音量值
     */
    fun getVolume(): Int

    /**
     * 获取办法给总时长，毫秒
     *
     * @return 视频总时长ms
     */
    fun getDuration(): Long

    /**
     * 获取当前播放的位置，毫秒
     *
     * @return 当前播放位置，ms
     */
    fun getCurrentPosition(): Long


    fun releasePlayer()

    fun isPreLoading(): Boolean
    fun isPreLoaded(): Boolean
    fun isIdle(): Boolean
    fun isPreparing(): Boolean
    fun isPrepared(): Boolean
    fun isBufferingPlaying(): Boolean
    fun isBufferingPaused(): Boolean
    fun isPlaying(): Boolean
    fun isPaused(): Boolean
    fun isError(): Boolean
    fun isCompleted(): Boolean

}