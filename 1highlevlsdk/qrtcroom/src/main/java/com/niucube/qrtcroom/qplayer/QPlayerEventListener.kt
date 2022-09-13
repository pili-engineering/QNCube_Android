package com.niucube.qrtcroom.qplayer

/**
 * 拉流事件回调
 */
interface QPlayerEventListener {
    /**
     * 拉流器准备中
     *
     * @param preparedTime 准备耗时
     */
    fun onPrepared(preparedTime: Int)

    /**
     * 拉流器信息回调
     *
     * @param what  事件 参考七牛霹雳播放器
     * @param extra 数据
     */
    fun onInfo(what: Int, extra: Int)

    /**
     * 拉流缓冲跟新
     *
     * @param percent 缓冲比分比
     */
    fun onBufferingUpdate(percent: Int)

    /**
     * /视频尺寸变化回调
     *
     * @param width  变化后的宽
     * @param height 变化后高
     */
    fun onVideoSizeChanged(width: Int, height: Int)

    /**
     * 播放出错回调
     *
     * @param errorCode 错误码 参考七牛霹雳播放器
     * @return 是否处理
     */
    fun onError(errorCode: Int): Boolean
}