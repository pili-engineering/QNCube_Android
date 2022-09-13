package com.niucube.player


import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.Surface
import com.niucube.player.utils.PalyerUtil

abstract class AbsPlayerEngine(protected val context: Context) : IPlayer {

    private var listeners = ArrayList<PlayerStatusListener>()
    protected var mPlayerConfig = PlayerConfig()
    private var originUri: Uri? = null
    protected var mCurrentState = PlayerStatus.STATE_IDLE
    private var isLossPause = false

    var tagNam =""

    companion object {
        const val media_Player = 1
        const val IJK_Player = 2
    }

    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->

            when (focusChange) {

                // AUDIOFOCUS_GAIN：你已经获得音频焦点；
                // AUDIOFOCUS_LOSS：你已经失去音频焦点很长时间了，必须终止所有的音频播放。因为长时间的失去焦点后，不应该在期望有焦点返回，这是一个尽可能清除不用资源的好位置。例如，应该在此时释放MediaPlayer对象；
                //AUDIOFOCUS_LOSS_TRANSIENT：这说明你临时失去了音频焦点，但是在不久就会再返回来。此时，你必须终止所有的音频播放，但是保留你的播放资源，因为可能不久就会返回来。
                // AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK：这说明你已经临时失去了音频焦点，但允许你安静的播放音频（低音量），而不是完全的终止音频播放

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT//Pause playback
                -> {
                    if (isPlaying()) {
                        pause()
                        isLossPause = true
                    }
                }
                AudioManager.AUDIOFOCUS_GAIN//Resume playback
                -> {
                    if (isLossPause && isPaused()) {
                        resume()
                    }
                    isLossPause = false
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK//
                -> {
                }
                AudioManager.AUDIOFOCUS_LOSS//Stop playback
                -> {

                }
            }
        }

    private val mAudioManager: AudioManager by lazy {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager
    }

    val mPlayerStatusListener = object : PlayerStatusListener {
        override fun onPlayModeChanged(model: Int) {
            listeners.forEach {
                it.onPlayModeChanged(model)
            }
        }

        override fun onPlayStateChanged(status: Int) {
            listeners.forEach {
                it.onPlayStateChanged(status)
            }
        }
    }

    final override fun getCurrentUrl(): Uri? {
        return originUri
    }


    final override fun setUp(uir: Uri, headers: Map<String, String>?, preLoading: Boolean) {
        val str = uir.toString()
        originUri = uir
        setUpAfterDealUrl(uir, headers, preLoading)
    }

    protected fun reqestFouces() {
        isLossPause = false
        mAudioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    protected fun savePotion() {
        if (isPlaying() || isBufferingPlaying() || isBufferingPaused() || isPaused()) {
            PalyerUtil.savePlayPosition(context, originUri?.path, getCurrentPosition().toInt())
        } else if (isCompleted()) {
            PalyerUtil.savePlayPosition(context, originUri?.path, 0)
        }
    }

    protected fun getLastPosition(): Int {
        return PalyerUtil.getSavedPlayPosition(context, originUri?.path)
    }


    abstract fun setUpAfterDealUrl(uir: Uri, headers: Map<String, String>?, preLoading: Boolean)


    override fun setPlayerConfig(playerConfig: PlayerConfig) {
        mPlayerConfig = playerConfig
        readPlayerConfig()
    }

    override fun getPlayerConfig(): PlayerConfig {
        return mPlayerConfig
    }

    override fun releasePlayer() {
        mCurrentState = PlayerStatus.STATE_IDLE
        mAudioManager.abandonAudioFocus(audioFocusChangeListener)
        mPlayerStatusListener.onPlayStateChanged(mCurrentState)
        savePotion()
        Runtime.getRuntime().gc()
        listeners.clear()
    }

    abstract fun readPlayerConfig()

    override fun addPlayStatusListener(lister: PlayerStatusListener, add: Boolean) {
        if (add) {
            listeners.add(lister)
        } else {
            listeners.remove(lister)
        }
    }

    abstract fun setSurface(surface: Surface)

    abstract fun setOnVideoSizeChangedListener(videoSizeChangedListener: OnVideoSizeChangedListener)


    interface OnVideoSizeChangedListener{
        fun onVideoSizeChanged(mp: AbsPlayerEngine, width: Int, height: Int)
        fun onRotationInfo(rotation:Float);
    }

    override fun isIdle(): Boolean {
        return mCurrentState == PlayerStatus.STATE_IDLE
    }

    override fun isPreparing(): Boolean {
        return mCurrentState == PlayerStatus.STATE_PREPARING
    }

    override fun isPrepared(): Boolean {
        return mCurrentState == PlayerStatus.STATE_PREPARED
    }

    override fun isBufferingPlaying(): Boolean {
        return mCurrentState == PlayerStatus.STATE_BUFFERING_PLAYING
    }

    override fun isBufferingPaused(): Boolean {
        return mCurrentState == PlayerStatus.STATE_BUFFERING_PAUSED
    }


    override fun isPaused(): Boolean {
        return mCurrentState == PlayerStatus.STATE_PAUSED || isBufferingPaused()
    }

    override fun isError(): Boolean {
        return mCurrentState == PlayerStatus.STATE_ERROR
    }

    override fun isCompleted(): Boolean {
        return mCurrentState == PlayerStatus.STATE_COMPLETED
    }

    override fun isPreLoading(): Boolean {
        return mCurrentState == PlayerStatus.STATE_PRELOADING
    }

    override fun isPreLoaded(): Boolean {
        return mCurrentState == PlayerStatus.STATE_PRELOADED_WAITING
    }


    override fun setVolume(volume: Int) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }

    override fun getCurrentPlayStatus(): Int {
        return mCurrentState;
    }

    override fun getMaxVolume(): Int {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    override fun getVolume(): Int {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }


}
