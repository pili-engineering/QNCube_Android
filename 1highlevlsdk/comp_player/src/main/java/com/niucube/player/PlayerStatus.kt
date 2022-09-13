package com.niucube.player

object PlayerStatus {
    /**
     * 播放未开始
     */
    const val STATE_IDLE = -1

    /**
     * 播放预装载中
     */
    const val STATE_PRELOADING = 0

    /**
     * 播放准备中
     */
    const val STATE_PREPARING = 1

    /**
     * 播放准备就绪
     */
    const val STATE_PREPARED = 2

    /**
     * 预装载完成　等等通知播放
     */
    const val STATE_PRELOADED_WAITING = 3
    const val STATE_STOP = 4

    /**
     * 正在播放
     */
    const val STATE_PLAYING = 5

    /**
     * 暂停播放
     */
    const val STATE_PAUSED = 6

    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
     */
    const val STATE_BUFFERING_PLAYING = 7

    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
     */
    const val STATE_BUFFERING_PAUSED = 8

    /**
     * 播放完成
     */
    const val STATE_COMPLETED = 9

    /**
     * 播放错误
     */
    const val STATE_ERROR = 10

    /**
     * 普通模式
     */
    const val MODE_NORMAL = 21

    /**
     * 全屏模式
     */
    const val MODE_FULL_SCREEN = 22

    /**
     * 小窗口模式
     */
    const val MODE_TINY_WINDOW = 23
}