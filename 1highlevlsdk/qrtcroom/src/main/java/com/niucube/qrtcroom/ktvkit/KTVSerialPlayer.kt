package com.niucube.ktvkit

import android.util.Log
import com.niucube.ktvkit.KTVMusic.Companion.playStatus_completed
import com.niucube.ktvkit.KTVMusic.Companion.playStatus_error
import com.niucube.ktvkit.KTVMusic.Companion.playStatus_pause
import com.niucube.ktvkit.KTVMusic.Companion.playStatus_playing
import com.niucube.ktvkit.KTVPlayerListener.Companion.owner_rtc_mix_error
import com.qiniu.droid.rtc.QNAudioMixer
import com.qiniu.droid.rtc.QNAudioMixerListener
import com.qiniu.droid.rtc.QNAudioMixerState
import com.qiniu.droid.rtc.QNMicrophoneAudioTrack
import java.io.File


open class KTVSerialPlayer<T>(
    val uid: String = "",
    val adapter: KTVSerialPlayerAdapter<T>,
) : IkTVPlayer<T> {

    var mMicrophoneAudioTrack: QNMicrophoneAudioTrack? = null

    interface KTVSerialPlayerAdapter<T> {
        /**
         * 怎么发音乐信令
         */
        fun sendKTVMusicSignal(
            music: KTVMusic<T>,
            callback: (isSuccess: Boolean, errorCode: Int, errorMsg: String) -> Unit
        )

        /**
         * 收到音乐信令的回调
         */
        fun registerSignalReceiveChannel(channel: (music: KTVMusic<T>) -> Unit)

        /**
         * 怎么初始化获得当前播放的音乐
         */
        fun initCurrentPlayingMusic(call: (music: KTVMusic<T>?) -> Unit)

        /**
         * 保存当前播放进度
         */
        fun saveCurrentPlayingMusicToServer(music: KTVMusic<T>)
    }

    companion object {
        val key_current_music = "ktv_current_music"
    }


    private var tracks: List<MusicTrack>? = null

    var mKTVMusic: KTVMusic<T>? = null
        private set

    private val mKTVPlayerListeners = ArrayList<KTVPlayerListener<T>>()

    override fun addKTVPlayerListener(ktvPlayerListener: KTVPlayerListener<T>) {
        mKTVPlayerListeners.add(ktvPlayerListener)
    }

    override fun removeKTVPlayerListener(ktvPlayerListener: KTVPlayerListener<T>) {
        mKTVPlayerListeners.remove(ktvPlayerListener)
    }

    init {
        adapter.registerSignalReceiveChannel { musicAttribute ->
            val isPauseLast = mKTVMusic?.playStatus ?: -1 == playStatus_pause
            if (mKTVMusic == null
                ||
                mKTVMusic?.musicId != musicAttribute.musicId
            ) {
                mKTVMusic = musicAttribute
                if (mKTVMusic?.mixerUid != uid) {
                    mKTVPlayerListeners.forEach {
                        it.onStart(musicAttribute)
                    }
                }
            } else {
                if (mKTVMusic != null
                    && mKTVMusic?.mixerUid != uid
                    && mKTVMusic?.trackType != musicAttribute.trackType
                ) {
                    mKTVPlayerListeners.forEach {
                        it.onSwitchTrack(musicAttribute.trackType.toTrackType())
                    }
                }
                mKTVMusic = musicAttribute
            }
            changeMusicAttributes(isPauseLast)
        }
    }

    public fun onJoinRoom() {
        adapter.initCurrentPlayingMusic { musicAttribute ->
            musicAttribute ?: return@initCurrentPlayingMusic
            mKTVMusic = musicAttribute
            mKTVPlayerListeners.forEach {
                it.onStart(musicAttribute!!)
            }
            changeMusicAttributes(false)
        }
    }

    private fun changeMusicAttributes(isPauseLast: Boolean) {

        if (mKTVMusic?.mixerUid != uid) {
            when (mKTVMusic!!.playStatus) {
                KTVMusic.playStatus_pause -> {
                    mKTVPlayerListeners.forEach {
                        it.onPause()
                    }
                }
                KTVMusic.playStatus_playing -> {
                    if (isPauseLast) {
                        mKTVPlayerListeners.forEach {
                            it.onResume()
                        }
                    }
                }
                KTVMusic.playStatus_error -> {
                    mKTVPlayerListeners.forEach {
                        it.onError(owner_rtc_mix_error, "主唱混音错误")
                    }
                }
            }
            if (mKTVMusic!!.currentPosition >= mKTVMusic!!.duration) {
                mKTVPlayerListeners.forEach {
                    it.onPlayCompleted()
                }
            }
            mKTVPlayerListeners.forEach {
                it.updatePosition(
                    mKTVMusic!!.currentPosition,//+ System.currentTimeMillis() - mKTVMusic!!.currentTimeMillis,
                    mKTVMusic!!.duration
                )
            }
        }
    }

    private var microphoneVolume = 100f
    private var musicVolume = 100f
    override fun setMicrophoneVolume(volume: Int) {
        microphoneVolume = volume.toFloat()
        mQNAudioMixer?.setMixingVolume(microphoneVolume / 100F, musicVolume / 100F)

    }

    override fun setMusicVolume(volume: Int) {
        musicVolume = volume.toFloat()
        mQNAudioMixer?.setMixingVolume(microphoneVolume / 100F, musicVolume / 100F)
    }

    override fun getMusicVolume(): Int {
        return musicVolume.toInt()
    }

    override fun getMicrophoneVolume(): Int {
        return microphoneVolume.toInt()
    }

    private var mQNAudioMixer: QNAudioMixer? = null

    val audioMixerListener = object : QNAudioMixerListener {

        override fun onStateChanged(p0: QNAudioMixerState) {
            when (p0) {
                QNAudioMixerState.MIXING -> {
                   // mQNAudioMixer?.enableEarMonitor(true)
                }
                QNAudioMixerState.COMPLETED -> {
                    mKTVMusic!!.playStatus = playStatus_completed

                    adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
                    adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                    mKTVPlayerListeners.forEach {
                        it.onPlayCompleted()
                    }
                }
                QNAudioMixerState.PAUSED -> {
                    mKTVMusic!!.playStatus = playStatus_pause

                    adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
                    adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                    mKTVPlayerListeners.forEach {
                        it.onPause()
                    }

                }
                QNAudioMixerState.STOPPED -> {
                   // mQNAudioMixer?.enableEarMonitor(false)
                }
            }
        }

        override fun onMixing(p0: Long) {
            Log.d("QNAudioMixingManager", "onMixing  " + p0)
            if (mKTVMusic != null) {
                mKTVMusic!!.playStatus = playStatus_playing
                mKTVMusic!!.currentPosition = p0 / 1000
                mKTVMusic!!.currentTimeMillis = System.currentTimeMillis()
                adapter.sendKTVMusicSignal(mKTVMusic!!) { isSuccess: Boolean, errorCode: Int, errorMsg: String ->
                }
                mKTVPlayerListeners.forEach {
                    it.updatePosition(p0 / 1000, mKTVMusic!!.duration)
                }
            }
        }

        override fun onError(p0: Int) {
           // mQNAudioMixer?.enableEarMonitor(false)
            Log.d("QNAudioMixingManager", "onError")
            mKTVMusic?.playStatus = playStatus_error
            adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
            adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
            mKTVPlayerListeners.forEach {
                it.onError(p0, "rtc mix error")
            }
        }
    }

    private var loopTime: Int = 0
    open override fun start(
        uid: String,
        musicId: String,
        loopTime: Int,
        tracks: List<MusicTrack>,
        musicInfo: T
    ) {
        this.tracks = tracks
        this.loopTime = loopTime

        val f = File(tracks[0].trackLocalFilePath)
        Log.d("QNAudioMixingManager", "f " + f.exists())
        var isFirstMIXING = true
        val mQNAudioMixerListener = object : QNAudioMixerListener {
            override fun onStateChanged(p0: QNAudioMixerState) {

                if (p0 == QNAudioMixerState.MIXING) {
                    if (!isFirstMIXING && mKTVMusic!!.playStatus == playStatus_pause) {
                        mKTVMusic!!.playStatus = playStatus_playing

                        adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
                        adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                        mKTVPlayerListeners.forEach {
                            it.onResume()
                        }
                    } else {
                        val music = KTVMusic<T>().apply {
                            this.musicId = musicId
                            mixerUid = uid
                            //开始播放的时间戳
                            startTimeMillis = System.currentTimeMillis()
                            currentPosition = mQNAudioMixer!!.currentPosition / 1000
                            currentTimeMillis = System.currentTimeMillis()
                            //播放状态 0 暂停  1 播放  2 出错
                            playStatus = 1
                            //音乐总长度
                            this.duration = mQNAudioMixer!!.duration / 1000
                            //播放的歌曲信息
                            trackType = tracks[0].trackType.value
                            this.musicInfo = musicInfo
                        }
                        mKTVMusic = music
                        adapter.saveCurrentPlayingMusicToServer(music)
                        adapter.sendKTVMusicSignal(music) { isSuccess: Boolean, errorCode: Int, errorMsg: String ->
                            if (isSuccess) {
                                mKTVPlayerListeners.forEach {
                                    it.onStart(music)
                                }
                            } else {
                                mQNAudioMixer!!.stop()
                                mKTVPlayerListeners.forEach {
                                    it.onError(errorCode, errorMsg)
                                }
                            }
                        }
                    }
                    isFirstMIXING = false
                }
                audioMixerListener.onStateChanged(p0)
            }

            override fun onMixing(p0: Long) {
                Log.d("QNAudioMixingManager", "onMixing  " + p0)
                audioMixerListener.onMixing(p0)
            }

            override fun onError(p0: Int) {
                Log.d("QNAudioMixingManager", "onError")
                audioMixerListener.onError(p0)
            }
        }
        mQNAudioMixer = mMicrophoneAudioTrack?.createAudioMixer(
            tracks[0].trackLocalFilePath,
            mQNAudioMixerListener
        )
        mQNAudioMixer?.start(loopTime)
        setMusicVolume(musicVolume.toInt())
        setMicrophoneVolume(microphoneVolume.toInt())

    }

    /**
     * 切换播放音轨
     */
    open override fun switchTrack(trackType: TrackType) {
        if (mKTVMusic?.trackType == trackType.value) {
            return
        }
        var isFirstMIXING = true
        tracks?.forEach {
            if (it.trackType == trackType) {
                mQNAudioMixer?.stop()
                val mQNAudioMixerListener = object : QNAudioMixerListener {
                    override fun onStateChanged(p0: QNAudioMixerState) {
                        if (p0 == QNAudioMixerState.MIXING) {
                            if (!isFirstMIXING && mKTVMusic!!.playStatus == playStatus_pause) {
                                mKTVMusic!!.playStatus = playStatus_playing

                                adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
                                adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                                mKTVPlayerListeners.forEach {
                                    it.onResume()
                                }
                            } else {
                                mQNAudioMixer?.seekTo(mKTVMusic!!.currentPosition * 1000)
                                mKTVMusic!!.playStatus = playStatus_playing
                                mKTVMusic!!.trackType = trackType.value
                                //mKTVMusic!!.currentPosition = mQNAudioMixingManager.currentTime
                                //   mKTVMusic!!.currentTimeMillis = System.currentTimeMillis()
                                adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
                                adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                                mKTVPlayerListeners.forEach {
                                    it.onSwitchTrack(trackType)
                                }
                            }
                            isFirstMIXING = false
                        }
                        audioMixerListener.onStateChanged(p0)
                    }

                    override fun onMixing(p0: Long) {
                        Log.d("QNAudioMixingManager", "onMixing  " + p0)
                        audioMixerListener.onMixing(p0)
                    }

                    override fun onError(p0: Int) {
                        Log.d("QNAudioMixingManager", "onError")
                        audioMixerListener.onError(p0)
                    }
                }
                mQNAudioMixer = mMicrophoneAudioTrack?.createAudioMixer(
                    it.trackLocalFilePath,
                    mQNAudioMixerListener
                )

                mQNAudioMixer?.start(loopTime)
                mQNAudioMixer?.setMixingVolume(microphoneVolume, musicVolume)
                return@forEach
            }
        }
    }

    open override fun seekTo(position: Long) {
        if (mKTVMusic == null) {
            return
        }
        mQNAudioMixer?.seekTo(position)
    }

    open override fun pause() {
        if (mKTVMusic == null) {
            return
        }
        mQNAudioMixer?.pause()
    }

    open override fun resume() {
        if (mKTVMusic == null) {
            return
        }
        mQNAudioMixer?.resume()
    }

    open override fun releasePlayer() {
        try {
            mQNAudioMixer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun enableEarMonitor(enable: Boolean){
        mQNAudioMixer?.enableEarMonitor(enable)
    }

}