package com.niucube.qrtcroom.qplayer

import android.content.Context
import android.util.Log
import android.view.Surface
import com.niucube.qrtcroom.liblog.QLiveLogUtil
import com.pili.pldroid.player.PLMediaPlayer
import com.pili.pldroid.player.PLOnErrorListener
import com.pili.pldroid.player.PLOnErrorListener.ERROR_CODE_OPEN_FAILED
import com.pili.pldroid.player.PLOnInfoListener
import com.pili.pldroid.player.PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING
import com.pili.pldroid.player.PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING
import com.pili.pldroid.player.PLOnPreparedListener
import com.pili.pldroid.player.PLOnVideoSizeChangedListener
import com.pili.pldroid.player.PlayerState

class QMediaPlayer(val context: Context) :QIPlayer {

    private var mPlayerEventListener:QPlayerEventListener? = null
    private var currentUrl = ""
    private var isRelease = false
    private var lastW = 0
    private var lastH = 0
    private var mIMediaPlayer: PLMediaPlayer? = null

    private fun resetPlayer() {
        mIMediaPlayer = PLMediaPlayer(
            context,
            QMediaPlayerConfig.mAVOptionsGetter.invoke()
        )
        mIMediaPlayer?.isLooping = false
        mIMediaPlayer?.setOnPreparedListener(mOnPreparedListener)
        mIMediaPlayer?.setOnErrorListener(mOnErrorListener)
        mIMediaPlayer?.setOnInfoListener(mOnInfoListener)
        mIMediaPlayer?.setOnVideoSizeChangedListener(mPLOnVideoSizeChangedListener)
    }

    override fun release() {
        isRelease = true
        currentUrl = ""
        mPlayerEventListener = null
        mIMediaPlayer?.release()
        if (mRenderView is QPlayerTextureRenderView) {
            (mRenderView as QPlayerTextureRenderView).stopPlayback()
        }
        mSurface = null
    }

    //切换rtc模式为了下麦快速恢复保持链接
    override fun onLinkStatusChange(isLink: Boolean) {
        if (isLink) {
            mIMediaPlayer?.stop()
            mIMediaPlayer?.release()
            mIMediaPlayer = null
        } else {
            resetPlayer()
            mIMediaPlayer?.setDataSource(currentUrl)
            start()
        }
    }

    override fun setUp(uir: String, headers: Map<String, String>?) {
        currentUrl = uir
        mIMediaPlayer?.stop()
    }

    override fun start() {
        QLiveLogUtil.d(
            "mIMediaPlayer",
            "start ${mIMediaPlayer?.isPlaying}   ${mIMediaPlayer?.dataSource == currentUrl}  ${mIMediaPlayer?.playerState?.name}"
        )
        if (mIMediaPlayer?.isPlaying == true &&
            mIMediaPlayer?.dataSource == currentUrl
        ) {
            return
        }
        if (mIMediaPlayer == null) {
            return
        }
        if (mIMediaPlayer?.playerState == PlayerState.ERROR) {
            mIMediaPlayer?.release()
            resetPlayer()
        }
        if (mIMediaPlayer?.playerState == PlayerState.BUFFERING && mSurface == null && mIMediaPlayer?.dataSource == currentUrl) {
            return
        }
        mIMediaPlayer?.dataSource = currentUrl
        try {
            QLiveLogUtil.d(
                "mIMediaPlayer",
                "start 从新设置播放"
            )
            mSurface?.let {
                mIMediaPlayer?.setSurface(it)
            }
            mIMediaPlayer?.prepareAsync()
            mIMediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 暂停
     */
    override fun pause() {
        Log.d("mIMediaPlayer", "pause")
        if (currentUrl == "" || isRelease) {
            return
        }
        mIMediaPlayer?.pause()
    }

    override fun stop() {
        Log.d("mIMediaPlayer", "stop")
        mIMediaPlayer?.stop()
    }

    /**
     * 恢复
     */
    override fun resume() {
        Log.d("mIMediaPlayer", "resume")
        if (currentUrl == "" || isRelease) {
            return
        }
        mIMediaPlayer?.start()
    }

    fun setEventListener(listener:QPlayerEventListener) {
        this.mPlayerEventListener = listener
    }

    private var mSurface: Surface? = null
    private var mRenderView:QPlayerRenderView? = null
    private var mQRenderCallback: QRenderCallback = object : QRenderCallback {
        override fun onSurfaceCreated(var1: Surface, var2: Int, var3: Int) {
            Log.d("mIMediaPlayer", "onSurfaceCreated")
            mSurface = var1
            mIMediaPlayer?.setSurface(mSurface)
        }

        override fun onSurfaceChanged(var1: Surface, var2: Int, var3: Int) {
            Log.d("mIMediaPlayer", "onSurfaceChanged")
            mSurface = var1
            mIMediaPlayer?.setSurface(mSurface)
        }

        override fun onSurfaceDestroyed(var1: Surface) {
            Log.d("mIMediaPlayer", "onSurfaceDestroyed")
            mSurface = null
        }
    }

    fun setPlayerRenderView(renderView:QPlayerRenderView) {
        this.mRenderView?.setRenderCallback(null)
        this.mRenderView = renderView
        renderView.setRenderCallback(mQRenderCallback)
        QLiveLogUtil.d(
            "mIMediaPlayer", "setPlayerRenderView  ${lastW} ${lastH}  "
        )
        if (lastW != 0 && lastH != 0) {
            mPLOnVideoSizeChangedListener.onVideoSizeChanged(lastW, lastH)
        }
        mSurface = renderView.getSurface()
        mIMediaPlayer?.setSurface(mSurface)

        QLiveLogUtil.d(
            "mIMediaPlayer", "mIMediaPlayer?.setSurface(mSurface) ${mSurface == null} "
        )
    }

    private fun isPlaying(): Boolean {
        return mIMediaPlayer?.isPlaying ?: false
    }

    private val mOnPreparedListener = PLOnPreparedListener { mp ->
        // mIMediaPlayer?.start()
        mPlayerEventListener?.onPrepared(mp)
    }
    private val mOnErrorListener = PLOnErrorListener { p0, p1 ->
        QLiveLogUtil.d(
            "mIMediaPlayer", "PLOnErrorListener  ${p0} ${p1}  "
        )
        if (p0 == ERROR_CODE_OPEN_FAILED) {
            QLiveLogUtil.d(
                "mIMediaPlayer", "ERROR_CODE_OPEN_FAILED restart  "
            )
           start()
        }
        mPlayerEventListener?.onError(p0) ?: false
    }

    private val mOnInfoListener = PLOnInfoListener { what, extra, _ ->
        if (what != MEDIA_INFO_VIDEO_FRAME_RENDERING &&
            what != MEDIA_INFO_AUDIO_FRAME_RENDERING
        ) {
            QLiveLogUtil.d(
                "mIMediaPlayer", "PLOnInfoListener  ${what} ${extra}  "
            )
        }
        mPlayerEventListener?.onInfo(what, extra)
    }
    private val mPLOnVideoSizeChangedListener =
        PLOnVideoSizeChangedListener { p0, p1 ->
            lastW = p0
            lastH = p1
            Log.d("mIMediaPlayer", "PLOnVideoSizeChangedListener  ${p0} ${p1}")
            if (mRenderView is QPlayerTextureRenderView) {
                (mRenderView as QPlayerTextureRenderView).setVideoSize(p0, p1)
            }
            if (mRenderView is QSurfaceRenderView) {
                (mRenderView as QSurfaceRenderView).setVideoSize(p0, p1)
            }
            mPlayerEventListener?.onVideoSizeChanged(p0, p1)
        }

    init {
        resetPlayer()
    }
}