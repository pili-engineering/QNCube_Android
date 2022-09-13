package com.niucube.ktvkit

interface KTVPlayerListener<T> {
    companion object {
        //主唱rtc混音错误
        val owner_rtc_mix_error = 1

        //获取歌曲信息错误
        val get_room_music_error = 2

        //下载失败
        val download_room_music_error = 3
    }

    //获取房间歌曲信息失败
    fun onError(errorCode: Int, msg: String)

    /**
     * 开始播放
     */
    fun onStart(ktvMusic: KTVMusic<T>)

    /**
     * 切换播放音轨
     */
    fun onSwitchTrack(trackType: TrackType)

    /**
     * 暂停
     */
    fun onPause()

    /**
     * 恢复
     */
    fun onResume()

    /**
     * 跟新播放进度
     */
    fun updatePosition(position: Long, duration: Long)

    /**
     * 播放完成
     */
    fun onPlayCompleted()
}