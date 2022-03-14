package com.niucube.playersdk

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.niucube.absroom.IAudiencePlayerView
import com.niucube.comproom.RoomEntity
import com.pili.pldroid.player.*
import com.pili.pldroid.player.PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START


class QNAudienceFitXYVideoPlayerView : FrameLayout, IAudiencePlayerView {

    private val mAVOptions = AVOptions().apply {
        setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        setInteger(AVOptions.KEY_FAST_OPEN, 1);
        setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 5);
        setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
    }
    private lateinit var mPLMediaPlayer: PLMediaPlayer
    private val mHandler = object : Handler(Looper.getMainLooper()) {}

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mPLMediaPlayer = PLMediaPlayer(context, mAVOptions)
        val surfaceView = SurfaceView(context)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                mPLMediaPlayer.setDisplay(surfaceView.holder)
                mPLMediaPlayer.setSurface(p0.surface)
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {

            }
        })
        addView(surfaceView)

        mPLMediaPlayer.setOnErrorListener(PLOnErrorListenerWrap())
        addOnErrorListener {it,_ ->
            if (it == -2) {
                if (toStart) {
                    mHandler.postDelayed(reStartRun, 3000)
                }
            }
            false
        }
        mPLMediaPlayer.setOnPreparedListener(object : PLOnPreparedListener {
            override fun onPrepared(p0: Int) {
                mPLMediaPlayer.start()
                Log.d("QNAudienceAudioPlayer", "onPrepared")
            }
        })
        mPLMediaPlayer.setOnInfoListener { i, i2 ,i3->
            Log.d("QNAudienceAudioPlayer", "setOnInfoListener" + i + "  " + i2)
        }
        mPLMediaPlayer.setOnInfoListener(PLOnInfoListenerWrap())
        addPLOnInfoListener { p0: Int, p1: Int ,_->
            if (p0 == PLOnInfoListener.MEDIA_INFO_CONNECTED) {
                if (!toStart) {
                    mPLMediaPlayer.stop()
                    visibility = View.INVISIBLE
                }
            }
            if (p0 == MEDIA_INFO_VIDEO_RENDERING_START && toStart) {
                visibility = View.VISIBLE
            }
        }
        visibility = View.INVISIBLE
    }


    private val mPLOnInfoListener = ArrayList<PLOnInfoListener>()

    private inner class PLOnInfoListenerWrap : PLOnInfoListener {
        override fun onInfo(p0: Int, p1: Int,p2:Any?) {
            mPLOnInfoListener.forEach {
                it.onInfo(p0, p1,p2)
            }
        }
    }

    //自动重试
    private val reStartRun = object : Runnable {
        override fun run() {
            if (toStart) {
                reStart()
            }
        }
    }
    private var mCurrentUrl = ""

    private var mPLOnErrorListeners = ArrayList<PLOnErrorListener>()

    private inner class PLOnErrorListenerWrap : PLOnErrorListener {
        override fun onError(p0: Int,any: Any?): Boolean {
            var deal = false
            Log.d("QNAudienceAudioPlayer", " setOnErrorListener ${p0}")
            mPLOnErrorListeners.forEach {
                if (it.onError(p0,any)) {
                    deal = true
                }
            }
            return deal
        }
    }

    fun addPLOnInfoListener(infoListener: PLOnInfoListener) {
        mPLOnInfoListener.add(infoListener)
    }

    fun removePLOnInfoListener(infoListener: PLOnInfoListener) {
        mPLOnInfoListener.remove(infoListener)
    }

    fun addOnErrorListener(listener: PLOnErrorListener) {
        mPLOnErrorListeners.add(listener)
    }

    fun removeOnErrorListener(listener: PLOnErrorListener) {
        mPLOnErrorListeners.remove(listener)
    }

    private var toStart = false

    /**
     * 开始播放拉流地址
     * 1角色变跟为拉流端观众
     * 2观众角色进入房间
     */
    override fun startAudiencePlay(roomEntity: RoomEntity) {
        visibility = View.INVISIBLE
        toStart = true
        mCurrentUrl = roomEntity.providePullUri()
        mPLMediaPlayer.dataSource = (roomEntity.providePullUri())
        mPLMediaPlayer.prepareAsync()
    }

    /**
     * 停止播放拉流地址
     * 1角色变跟为主播
     * 2用户角色房间离开销毁
     */
    override fun stopAudiencePlay() {
        visibility = View.INVISIBLE
        toStart = false
        mPLMediaPlayer.stop()
    }

    private fun reStart() {
        mPLMediaPlayer.dataSource = mCurrentUrl
        mPLMediaPlayer.prepareAsync()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPLMediaPlayer.stop()
        mPLMediaPlayer.release()
    }

}