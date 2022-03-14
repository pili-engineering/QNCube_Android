package com.niucube.playersdk.player.engine

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.Surface
import com.niucube.playersdk.player.AbsPlayerEngine
import com.niucube.playersdk.player.PlayerStatus
import com.niucube.playersdk.player.PlayerStatus.STATE_PRELOADED_WAITING
import com.niucube.playersdk.player.PlayerStatus.STATE_PREPARED
import com.niucube.playersdk.player.utils.LogUtil
import com.pili.pldroid.player.*
import com.pili.pldroid.player.PLOnErrorListener.ERROR_CODE_IO_ERROR
import com.pili.pldroid.player.PLOnErrorListener.ERROR_CODE_SEEK_FAILED

class PLEngine(context: Context) : AbsPlayerEngine(context) {

    private var mUrl: Uri? = null
    private var continueFromLastPosition = false
    private var mBufferPercentage: Int = 0
    private var mHeaders: Map<String, String>? = null
    private var isUsePreLoad = false

    companion object {
        private var STATIC_URL: Uri? = null
    }

    private val mIMediaPlayer: PLMediaPlayer by lazy {
        val m = PLMediaPlayer(context,
            if (getPlayerConfig().avOptions == null) {
                AVOptions().apply {
                    // setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
                    setInteger(AVOptions.KEY_FAST_OPEN, 1);
                    setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 5);
                    setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
                }
            } else {
                getPlayerConfig().avOptions
            }
        )
        m.isLooping = mPlayerConfig.loop
        m.setOnPreparedListener(mOnPreparedListener)
        m.setOnCompletionListener(mOnCompletionListener)
        m.setOnErrorListener(mOnErrorListener)
        m.setOnInfoListener(mOnInfoListener)
        m.setOnBufferingUpdateListener(mOnBufferingUpdateListener)
        m.setOnVideoSizeChangedListener(mPLOnVideoSizeChangedListener)
        m
    }

    private var mSurface: Surface? = null
    override fun setSurface(surface: Surface) {
        mSurface = surface
        mIMediaPlayer.setSurface(surface)
    }

    override fun readPlayerConfig() {
        continueFromLastPosition = mPlayerConfig.isFromLastPosition
        mIMediaPlayer.isLooping = mPlayerConfig.loop
    }

    override fun setUpAfterDealUrl(uir: Uri, headers: Map<String, String>?, preLoading: Boolean) {
        mUrl = uir
        mHeaders = headers
        isUsePreLoad = preLoading
        if (isUsePreLoad) {
            mIMediaPlayer.addIOCache(mUrl.toString())
            openMedia()
        }
    }

    override fun startPlay() {
        STATIC_URL = mUrl
        if (mCurrentState == PlayerStatus.STATE_PRELOADED_WAITING || mCurrentState == PlayerStatus.STATE_PREPARED) {
            LogUtil.d(tagNam + "预加载　调用了start 准备好了 ——>      noticePreLoading   " + mCurrentState)
            startCall.invoke()
            return
        }
        if (mCurrentState == PlayerStatus.STATE_PRELOADING) {
            LogUtil.d(tagNam + "预加载　调用了start 还在准备 ——>     noticePreLoading   " + mCurrentState)
            isUsePreLoad = false
            mCurrentState = PlayerStatus.STATE_PREPARING
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            return
        }
        isUsePreLoad = false
        openMedia()
    }

    private var mVideoSizeChangedListener: OnVideoSizeChangedListener? = null
    private val mPLOnVideoSizeChangedListener = object : PLOnVideoSizeChangedListener {
        override fun onVideoSizeChanged(p0: Int, p1: Int) {
            mVideoSizeChangedListener?.onVideoSizeChanged(this@PLEngine, p0, p1)
        }
    }

    override fun setOnVideoSizeChangedListener(videoSizeChangedListener: OnVideoSizeChangedListener) {
        mVideoSizeChangedListener = videoSizeChangedListener
    }

    private fun openMedia() {
        try {
            savePotion()
            mIMediaPlayer.stop()
            //  mIMediaPlayer.reset()
            mCurrentState = if (isUsePreLoad) {
                PlayerStatus.STATE_PRELOADING
            } else {
                PlayerStatus.STATE_PREPARING
            }
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            //  mIMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
            mIMediaPlayer.setDataSource(mUrl?.toString(), mHeaders)
            mSurface?.let {
                mIMediaPlayer.setSurface(it)
            }
            mIMediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            mCurrentState = PlayerStatus.STATE_ERROR
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            LogUtil.d(tagNam + "            mIMediaPlayer.prepareAsync() 准备失败")
        }
    }

    override fun pause() {
        mIMediaPlayer.pause()
        mCurrentState = PlayerStatus.STATE_PAUSED
        mPlayerStatusListener.onPlayStateChanged(mCurrentState)
        LogUtil.d(tagNam + "STATE_PAUSED")
    }

    override fun stop() {
        mIMediaPlayer.stop()
        mCurrentState = PlayerStatus.STATE_STOP
        mPlayerStatusListener.onPlayStateChanged(mCurrentState)
    }

    override fun resume() {

        if (mCurrentState == PlayerStatus.STATE_PAUSED || mCurrentState == PlayerStatus.STATE_STOP) {
            mIMediaPlayer.start()
            mCurrentState = PlayerStatus.STATE_PLAYING
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            LogUtil.d(tagNam + "STATE_PLAYING")
        } else if (mCurrentState == PlayerStatus.STATE_BUFFERING_PAUSED) {
            mIMediaPlayer.start()
            mCurrentState = PlayerStatus.STATE_BUFFERING_PLAYING
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            LogUtil.d(tagNam + "STATE_BUFFERING_PLAYING")
        } else if (mCurrentState == PlayerStatus.STATE_COMPLETED || mCurrentState == PlayerStatus.STATE_ERROR) {
            mIMediaPlayer.stop()
            openMedia()
        } else {
            LogUtil.d(tagNam + "VideoPlayer在mCurrentState == " + mCurrentState + "时不能调用restart()方法.")
        }
    }

    override fun seekTo(pos: Int) {
        mIMediaPlayer.seekTo(pos.toLong())
    }

    override fun getDuration(): Long {
        return mIMediaPlayer.duration.toLong()
    }

    override fun getBufferPercentage(): Int {
        return mBufferPercentage
    }

    override fun getCurrentPosition(): Long {
        return mIMediaPlayer.currentPosition.toLong()
    }

    override fun releasePlayer() {
        super.releasePlayer()
        mIMediaPlayer.release()
    }

    private val startCall = {
        reqestFouces()
        isUsePreLoad = false
        mIMediaPlayer.start()
        LogUtil.d("mIMediaPlayer.start() mIMediaPlayer.start()")
        mCurrentState = PlayerStatus.STATE_PLAYING
        mPlayerStatusListener.onPlayStateChanged(mCurrentState)
        // 从上次的保存位置播放
        if (continueFromLastPosition) {
            val savedPlayPosition = getLastPosition()
            if (savedPlayPosition > 0) {
                mIMediaPlayer.seekTo(savedPlayPosition.toLong())
            }
        } else {
            mIMediaPlayer.seekTo(0)
        }
    }
    private val mOnPreparedListener = PLOnPreparedListener { mp ->

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

    private val mOnCompletionListener = PLOnCompletionListener {
        mCurrentState = PlayerStatus.STATE_COMPLETED
        mPlayerStatusListener.onPlayStateChanged(mCurrentState)
        savePotion()
        LogUtil.d(tagNam + "onCompletion ——> STATE_COMPLETED")
    }

    private val mOnErrorListener = PLOnErrorListener { p0, _ ->
        LogUtil.d(tagNam +" PLOnErrorListener.PLOnErrorListener() ${p0}")
        if (p0 == ERROR_CODE_IO_ERROR || ERROR_CODE_SEEK_FAILED == p0) {
            true
        } else {
            mCurrentState = PlayerStatus.STATE_ERROR
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            true
        }
    }

    private val mOnInfoListener = PLOnInfoListener { what, extra, _ ->
        LogUtil.d("PLOnInfoListener", "onInfo ——>${what}")
        if(what== PLOnInfoListener.MEDIA_INFO_CONNECTED){
            LogUtil.d("onInfo MEDIA_INFO_CONNECTEDMEDIA_INFO_CONNECTEDMEDIA_INFO_CONNECTEDMEDIA_INFO_CONNECTED——>${what}")
        }
        if (what == PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START || what == PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START) {
            if (mCurrentState != PlayerStatus.STATE_PLAYING) {
                // 播放器开始渲染
                mCurrentState = PlayerStatus.STATE_PLAYING
                mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            }
            LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_VIDEO_RENDERING_START：STATE_PLAYING")
        } else if (what == PLOnInfoListener.MEDIA_INFO_BUFFERING_START) {
            // IMediaPlayer暂时不播放，以缓冲更多的数据
            if (mCurrentState == PlayerStatus.STATE_PAUSED || mCurrentState == PlayerStatus.STATE_BUFFERING_PAUSED) {
                mCurrentState = PlayerStatus.STATE_BUFFERING_PAUSED
                LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PAUSED")
            } else {
                mCurrentState = PlayerStatus.STATE_BUFFERING_PLAYING
                LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PLAYING")
            }
            mPlayerStatusListener.onPlayStateChanged(mCurrentState)
        } else if (what == PLOnInfoListener.MEDIA_INFO_BUFFERING_END) {
            // 填充缓冲区后，IMediaPlayer恢复播放/暂停
            if (mCurrentState == PlayerStatus.STATE_BUFFERING_PLAYING) {
                mCurrentState = PlayerStatus.STATE_PLAYING
                mPlayerStatusListener.onPlayStateChanged(mCurrentState)
                LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PLAYING")
            }
            if (mCurrentState == PlayerStatus.STATE_BUFFERING_PAUSED) {
                mCurrentState = PlayerStatus.STATE_PAUSED
                mPlayerStatusListener.onPlayStateChanged(mCurrentState)
                LogUtil.d(tagNam + "onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PAUSED")
            }
        } else if (what == PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
            // 视频旋转了extra度，需要恢复
//                if (mTextureView != null) {
//                    mTextureView.setRotation(extra)
//                    LogUtil.d(tagNam+"视频旋转角度：$extra")
//                }
            mVideoSizeChangedListener?.onRotationInfo(extra.toFloat())
        } else if (what == PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING || what == PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING) {
            if (mCurrentState == PlayerStatus.STATE_PREPARED) {
                mCurrentState = PlayerStatus.STATE_PLAYING
                mPlayerStatusListener.onPlayStateChanged(mCurrentState)
            }
        }
//
//            else if (what == PLOnInfoListener.MEDIA_INFO_NOT_SEEKABLE) {
//                LogUtil.d(tagNam + "视频不能seekTo，为直播视频")
//            } else {
//                LogUtil.d(tagNam + "onInfo ——> what：$what")
//            }
    }

    private val mOnBufferingUpdateListener =
        PLOnBufferingUpdateListener { p0 -> mBufferPercentage = p0 }

    override fun isPlaying(): Boolean {
        return mIMediaPlayer.isPlaying
    }

}