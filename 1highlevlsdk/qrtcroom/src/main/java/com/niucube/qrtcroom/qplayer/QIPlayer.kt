package com.niucube.qrtcroom.qplayer

/**
 * 播放器-
 * 监听连麦状态的播放器
 */
interface QIPlayer {
    fun setUp(uir: String, headers: Map<String, String>? = null)
    fun start()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 恢复
     */
    fun resume()
    fun stop()
    fun release()

    /**
     * 连麦状态变化
     * @param isLink
     */
    fun onLinkStatusChange(isLink: Boolean)
}