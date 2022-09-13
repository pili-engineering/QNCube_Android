package com.niucube.player.engine

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import com.niucube.player.AbsPlayerEngine
import com.niucube.player.PlayerStatus.STATE_BUFFERING_PAUSED
import com.niucube.player.PlayerStatus.STATE_BUFFERING_PLAYING
import com.niucube.player.PlayerStatus.STATE_COMPLETED
import com.niucube.player.PlayerStatus.STATE_ERROR
import com.niucube.player.PlayerStatus.STATE_PAUSED
import com.niucube.player.PlayerStatus.STATE_PLAYING
import com.niucube.player.PlayerStatus.STATE_PRELOADED_WAITING
import com.niucube.player.PlayerStatus.STATE_PRELOADING
import com.niucube.player.PlayerStatus.STATE_PREPARED
import com.niucube.player.PlayerStatus.STATE_PREPARING
import com.niucube.player.PlayerStatus.STATE_STOP
import com.niucube.player.utils.LogUtil

/**
 * 原生播放引擎
 */
internal class NativeEngine(context: Context) : AbsPlayerEngine(context) {

    private var mUrl: Uri? = null

    private var continueFromLastPosition = false
    private var mBufferPercentage: Int = 0
    private var mHeaders: Map<String, String>? = null
    private var isUsePreLoad = false

    companion object {
        private var STATIC_URL: Uri? = null
    }

    /**
     * 原生 播放器
     */
    private val mMediaPlayer: MediaPlayer by lazy {
        val m = MediaPlayer()
        m.setAudioStreamType(AudioManager.STREAM_MUSIC)
        m.setOnPreparedListener(mOnPreparedListener)
        m.setOnCompletionListener(mOnCompletionListener)
        m.setOnErrorListener(mOnErrorListener)
        m.setOnInfoListener(mOnInfoListener)
        m.setOnBufferingUpdateListener(mOnBufferingUpdateListener)

        m
    }


    override fun setOnVideoSizeChangedListener(videoSizeChangedListener: OnVideoSizeChangedListener) {

        mMediaPlayer.setOnVideoSizeChangedListener { mp, width, height ->
            if (STATIC_URL == mUrl) {
                videoSizeChangedListener.onVideoSizeChanged(this, width, height)
            }
        }
    }


    override fun setSurface(surface: Surface) {
        mMediaPlayer.setSurface(surface)
    }

    override fun readPlayerConfig() {
        continueFromLastPosition = mPlayerConfig.isFromLastPosition
        mMediaPlayer.isLooping = mPlayerConfig.loop
    }


    override fun setUpAfterDealUrl(uir: Uri, headers: Map<String, String>?, preLoading: Boolean) {
        mUrl = uir
        mHeaders = headers
        isUsePreLoad = preLoading
        if (isUsePreLoad) {
            openMedia()
        }
    }

    override fun startPlay() {
        STATIC_URL = mUrl
        if (mCurrentState == STATE_PRELOADED_WAITING || mCurrentState == STATE_PREPARED) {
            LogUtil.d(tagNam + "预加载　调用了start 准备好了 ——>      noticePreLoading   " + mCurrentState)
            startCall.invoke()
            return
        }
        if (mCurrentState == STATE_PRELOADING) {
            LogUtil.d(tagNam + "预加载　调用了start 还在准备 ——>     noticePreLoading   " + mCurrentState)
            isUsePreLoad = false
            mCurrentState = STATE_PREPARING
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            return
        }
        isUsePreLoad = false
        openMedia()
    }


    private fun openMedia() {

        try {
            savePotion()
            mMediaPlayer.stop()
            mMediaPlayer.reset()
            mMediaPlayer.isLooping = mPlayerConfig.loop
            mCurrentState = if (isUsePreLoad) {
                STATE_PRELOADING
            } else {
                STATE_PREPARING
            }
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            mMediaPlayer.setDataSource(context.applicationContext, mUrl!!, mHeaders)
            mMediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            mCurrentState = STATE_ERROR
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            LogUtil.d(tagNam + "            mMediaPlayer.prepareAsync() 准备失败")
        }
    }

    override fun pause() {
        mMediaPlayer.pause()
        mCurrentState = STATE_PAUSED
        mPlayerStatusListener.onPlayStateChanged(mCurrentState)
        LogUtil.d(tagNam + "STATE_PAUSED")
    }

    override fun stop() {
        mMediaPlayer.stop()
        mCurrentState = STATE_STOP
        mPlayerStatusListener.onPlayStateChanged(mCurrentState)
    }

    override fun resume() {

        if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_STOP) {
            mMediaPlayer.start()
            mCurrentState = STATE_PLAYING
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            LogUtil.d(tagNam + "STATE_PLAYING")
        } else if (mCurrentState == STATE_BUFFERING_PAUSED) {
            mMediaPlayer.start()
            mCurrentState = STATE_BUFFERING_PLAYING
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            LogUtil.d(tagNam + "STATE_BUFFERING_PLAYING")
        } else if (mCurrentState == STATE_COMPLETED || mCurrentState == STATE_ERROR) {
            mMediaPlayer.reset()
            openMedia()
        } else {
            LogUtil.d(tagNam + "VideoPlayer在mCurrentState == " + mCurrentState + "时不能调用restart()方法.")
        }
    }

    override fun seekTo(pos: Int) {
        mMediaPlayer.seekTo(pos)
    }

    override fun getDuration(): Long {
        return mMediaPlayer.duration.toLong()
    }

    override fun getBufferPercentage(): Int {
        return mBufferPercentage
    }

    override fun getCurrentPosition(): Long {
        return mMediaPlayer.currentPosition.toLong()
    }

    override fun releasePlayer() {
        super.releasePlayer()
        mMediaPlayer.release()
    }

    private val startCall = {
        if (STATIC_URL == mUrl) {
            reqestFouces()
            isUsePreLoad = false
            mMediaPlayer.start()
            // 从上次的保存位置播放
            if (continueFromLastPosition) {
                val savedPlayPosition = getLastPosition()
                if (savedPlayPosition > 0) {
                    mMediaPlayer.seekTo(savedPlayPosition.toInt())
                }
            }
        }
    }
    private val mOnPreparedListener = MediaPlayer.OnPreparedListener { mp ->

        if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_STOP) {
            mMediaPlayer.stop()
            return@OnPreparedListener
        }

        if (isUsePreLoad) {
            mCurrentState = STATE_PRELOADED_WAITING
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            LogUtil.d(tagNam + "onPrepared ——> STATE_PREPARED   wait noticePreLoading")
        } else {
            mCurrentState = STATE_PREPARED
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            LogUtil.d(tagNam + "onPrepared ——> STATE_PREPARED")
            startCall.invoke()
        }
    }

    private val mOnCompletionListener = MediaPlayer.OnCompletionListener {
        mCurrentState = STATE_COMPLETED
        mPlayerStatusListener.onPlayStateChanged(mCurrentState)
        savePotion()
        LogUtil.d(tagNam + "onCompletion ——> STATE_COMPLETED")

    }

    private val mOnErrorListener = object : MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            LogUtil.d(tagNam + "onError    what   " +what+"    extra "+extra )
            if(MediaPlayer.MEDIA_ERROR_IO == what || extra==MediaPlayer.MEDIA_ERROR_IO ){
                return true
            }
            mCurrentState = STATE_ERROR
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            return true
        }
    }

    private val mOnInfoListener = object : MediaPlayer.OnInfoListener {
        override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            LogUtil.d(tagNam + "onInfo    what   " +what+"    extra "+extra )
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // 播放器开始渲染
                if (mMediaPlayer.isPlaying) {
                    mCurrentState = STATE_PLAYING
                    mPlayerStatusListener.onPlayStateChanged(mCurrentState)
                    LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_VIDEO_RENDERING_START：STATE_PLAYING")
                } else {
                    LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_VIDEO_RENDERING_START：视频暂停中 但是切换旋转回调了播放")
                }
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                // MediaPlayer暂时不播放，以缓冲更多的数据
                if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                    mCurrentState = STATE_BUFFERING_PAUSED
                    LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PAUSED")
                } else {
                    mCurrentState = STATE_BUFFERING_PLAYING
                    LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PLAYING")
                }
                mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                // 填充缓冲区后，MediaPlayer恢复播放/暂停
                if (mCurrentState == STATE_BUFFERING_PLAYING) {
                    mCurrentState = STATE_PLAYING
                    mPlayerStatusListener.onPlayStateChanged(mCurrentState)
                    LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PLAYING")
                }
                if (mCurrentState == STATE_BUFFERING_PAUSED) {
                    mCurrentState = STATE_PAUSED
                    mPlayerStatusListener.onPlayStateChanged(mCurrentState)
                    LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PAUSED")
                }
            }
//            else if (what == MediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
//                // 视频旋转了extra度，需要恢复
//                if (mTextureView != null) {
//                    mTextureView.setRotation(extra)
//                    LogUtil.d(tagNam+"视频旋转角度：$extra")
//                }
//            }
//
            else if (what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
                LogUtil.d(tagNam + "视频不能seekTo，为直播视频")
            } else {
                LogUtil.d(tagNam + "onInfo ——> what：$what")
            }
            return true
        }
    }

    private val mOnBufferingUpdateListener =
            MediaPlayer.OnBufferingUpdateListener { mp, percent -> mBufferPercentage = percent }

    override fun isPlaying(): Boolean {
        return mMediaPlayer.isPlaying
    }

}