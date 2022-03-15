package com.niucube.playersdk

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.niucube.absroom.IAudiencePlayerView
import com.pili.pldroid.player.widget.PLVideoView
import com.niucube.comproom.RoomEntity
import com.pili.pldroid.player.AVOptions
import com.pili.pldroid.player.PLOnErrorListener
import com.pili.pldroid.player.PLOnInfoListener
import com.pili.pldroid.player.PLOnInfoListener.MEDIA_INFO_CONNECTED
import com.pili.pldroid.player.PLOnVideoSizeChangedListener

class QNAudienceVideoPlayerView : PLVideoView, IAudiencePlayerView {

    //自动重试
    private val reStartRun = object : Runnable {
        override fun run() {
            if (toStart) {
                reStart()
            }
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val options = AVOptions()
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        options.setInteger(AVOptions.KEY_FAST_OPEN, 1);
        options.setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 5);
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        setAVOptions(options)
        setOnErrorListener(PLOnErrorListenerWrap())
        addOnErrorListener {it,_ ->
            if (it == -2) {
                if (toStart) {
                    postDelayed(reStartRun, 3000)
                }
            }
            false
        }
        visibility = View.INVISIBLE
        setOnInfoListener(PLOnInfoListenerWrap())
        addPLOnInfoListener { p0: Int, p1: Int ,_->
            if (p0 == MEDIA_INFO_CONNECTED) {
                if(!toStart){
                    pause()
                    visibility = View.INVISIBLE
                }else{
                    visibility = View.VISIBLE
                }
            }
        }
    }

    private var mCurrentUrl = ""

    private var mPLOnErrorListeners = ArrayList<PLOnErrorListener>()

    private inner class PLOnErrorListenerWrap : PLOnErrorListener {
        override fun onError(p0: Int,any: Any?): Boolean {
            var deal = false
            Log.d("setOnErrorListener", " setOnErrorListener ${p0}")
            mPLOnErrorListeners.forEach {
                if (it.onError(p0,any)) {
                    deal = true
                }
            }
            return deal
        }
    }

    private val mPLOnInfoListener = ArrayList<PLOnInfoListener>()

    private inner class PLOnInfoListenerWrap : PLOnInfoListener {
        override fun onInfo(p0: Int, p1: Int,any: Any?) {
            mPLOnInfoListener.forEach {
                it.onInfo(p0, p1,any)
            }
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
        setVideoURI(Uri.parse(roomEntity.providePullUri()))
        start()
    }

    /**
     * 停止播放拉流地址
     * 1角色变跟为主播
     * 2用户角色房间离开销毁
     */
    override fun stopAudiencePlay() {
        visibility = View.GONE
        toStart = false
        pause()
    }

    private fun reStart() {
        setVideoURI(Uri.parse(mCurrentUrl))
        start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}