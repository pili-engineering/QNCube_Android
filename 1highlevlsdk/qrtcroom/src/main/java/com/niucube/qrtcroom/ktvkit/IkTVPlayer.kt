package com.niucube.qrtcroom.ktvkit

//播放器
interface IkTVPlayer<T> {

    fun setMicrophoneVolume(volume: Int)

    fun setMusicVolume(volume: Int)

    fun start(uid: String, musicId: String,loopTime:Int, tracks: List<MusicTrack>,musicInfo:T)
    /**
     * 切换播放音轨
     */
    fun switchTrack(trackType: TrackType)

    fun seekTo(position: Long)

    fun pause()

    fun resume()

    fun releasePlayer()

    fun addKTVPlayerListener(ktvPlayerListener: KTVPlayerListener<T>)

    fun removeKTVPlayerListener(ktvPlayerListener: KTVPlayerListener<T>)

     fun getMusicVolume(): Int

     fun getMicrophoneVolume(): Int
}