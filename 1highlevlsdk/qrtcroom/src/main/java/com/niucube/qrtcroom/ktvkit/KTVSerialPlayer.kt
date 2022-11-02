package com.niucube.qrtcroom.ktvkit

import android.util.Log
import com.niucube.qrtcroom.ktvkit.KTVMusic.Companion.playStatus_completed
import com.niucube.qrtcroom.ktvkit.KTVMusic.Companion.playStatus_error
import com.niucube.qrtcroom.ktvkit.KTVMusic.Companion.playStatus_pause
import com.niucube.qrtcroom.ktvkit.KTVMusic.Companion.playStatus_playing
import com.niucube.qrtcroom.ktvkit.KTVPlayerListener.Companion.owner_rtc_mix_error
import com.qiniu.droid.rtc.*
import com.qiniu.droid.rtc.QNErrorCode.ERROR_AUDIO_MIXING_SEEK_FAILED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            val isPauseLast = (mKTVMusic?.playStatus ?: -1) == playStatus_pause
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
    private var musicVolume = 60f
    override fun setMicrophoneVolume(volume: Int) {
        microphoneVolume = volume.toFloat()
        mMicrophoneAudioTrack?.setVolume(microphoneVolume / 100.0)
    }

    override fun setMusicVolume(volume: Int) {
        musicVolume = volume.toFloat()
        mQNAudioMixer?.mixingVolume = musicVolume / 100F
    }

    override fun getMusicVolume(): Int {
        return musicVolume.toInt()
    }

    override fun getMicrophoneVolume(): Int {
        return microphoneVolume.toInt()
    }

    private var mQNAudioMixer: QNAudioMusicMixer? = null

    val audioMixerListener = object : QNAudioMusicMixerListener {

        override fun onStateChanged(p0: QNAudioMusicMixerState) {
            when (p0) {
                QNAudioMusicMixerState.MIXING -> {
                    // mQNAudioMixer?.enableEarMonitor(true)
                }
                QNAudioMusicMixerState.COMPLETED -> {
                    mKTVMusic!!.playStatus = playStatus_completed

                    adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
                    adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                    mKTVPlayerListeners.forEach {
                        it.onPlayCompleted()
                    }
                }
                QNAudioMusicMixerState.PAUSED -> {
                    mKTVMusic!!.playStatus = playStatus_pause

                    adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
                    adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                    mKTVPlayerListeners.forEach {
                        it.onPause()
                    }
                }
                QNAudioMusicMixerState.STOPPED -> {
                    // mQNAudioMixer?.enableEarMonitor(false)
                }
            }
        }

        override fun onMixing(p0: Long) {
            Log.d("QNAudioMixingManager", "onMixing  " + p0)
            if (mKTVMusic != null) {
                mKTVMusic!!.playStatus = playStatus_playing
                mKTVMusic!!.currentPosition = p0
                mKTVMusic!!.currentTimeMillis = System.currentTimeMillis()
                adapter.sendKTVMusicSignal(mKTVMusic!!) { isSuccess: Boolean, errorCode: Int, errorMsg: String ->
                }
                mKTVPlayerListeners.forEach {
                    it.updatePosition(p0, mKTVMusic!!.duration)
                }
            }
        }

        override fun onError(p0: Int, p1: String) {
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

    private var loopTime: Int = 1
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
        val path = tracks[0].trackLocalFilePath
        //音乐总长度
        val duration = QNAudioMusicMixer.getDuration(path)

        val mQNAudioMixerListener = object : QNAudioMusicMixerListener {
            override fun onStateChanged(p0: QNAudioMusicMixerState) {
                Log.d("QNAudioMixingManager", "onStateChanged  " + p0.name)
                if (p0 == QNAudioMusicMixerState.MIXING) {
                    if (!isFirstMIXING && mKTVMusic!!.playStatus == playStatus_pause) {
                        mKTVMusic!!.playStatus = playStatus_playing
                        adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
                        adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                        mKTVPlayerListeners.forEach {
                            it.onResume()
                        }
                    } else {

                        setMusicVolume(musicVolume.toInt())
                        setMicrophoneVolume(microphoneVolume.toInt())
                        val music = KTVMusic<T>().apply {
                            this.musicId = musicId
                            mixerUid = uid
                            //开始播放的时间戳
                            startTimeMillis = System.currentTimeMillis()
                            currentPosition = mQNAudioMixer!!.currentPosition
                            currentTimeMillis = System.currentTimeMillis()
                            //播放状态 0 暂停  1 播放  2 出错
                            playStatus = 1
                            this.duration = duration
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

            override fun onError(p0: Int, p1: String) {
                Log.d("QNAudioMixingManager", "onError ${p0} ${p1}")
                audioMixerListener.onError(p0, p1)
            }
        }
        mQNAudioMixer = mMicrophoneAudioTrack?.createAudioMusicMixer(
            path,
            mQNAudioMixerListener
        )
        mQNAudioMixer?.start(loopTime)
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
                val orientationPosition = mQNAudioMixer?.currentPosition ?: 0L
                mQNAudioMixer?.stop()

                val mQNAudioMixerListener = object : QNAudioMusicMixerListener {
                    override fun onStateChanged(p0: QNAudioMusicMixerState) {
                        Log.d("QNAudioMixingManager", "onStateChanged  " + p0.name)
                        if (p0 == QNAudioMusicMixerState.MIXING) {
                            if (!isFirstMIXING && mKTVMusic!!.playStatus == playStatus_pause) {
                                mKTVMusic!!.playStatus = playStatus_playing
                                adapter.saveCurrentPlayingMusicToServer(mKTVMusic!!)
                                adapter.sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                                mKTVPlayerListeners.forEach {
                                    it.onResume()
                                }
                            } else if (isFirstMIXING) {
                                mKTVMusic!!.playStatus = playStatus_playing
                                mKTVMusic!!.trackType = trackType.value
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

                    override fun onError(p0: Int, msg: String) {
                        Log.d("QNAudioMixingManager", "onError ${p0} ${msg}")
                        audioMixerListener.onError(p0, msg)
                    }
                }
                val path = it.trackLocalFilePath
                val duration = QNAudioMusicMixer.getDuration(path)
                mQNAudioMixer = mMicrophoneAudioTrack?.createAudioMusicMixer(
                    path,
                    mQNAudioMixerListener
                )
                mQNAudioMixer?.startPosition = orientationPosition
                mQNAudioMixer?.start(loopTime)
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

    fun enableEarMonitor(enable: Boolean) {
        mMicrophoneAudioTrack?.isEarMonitorEnabled = enable
    }
}