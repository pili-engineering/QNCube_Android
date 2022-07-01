package com.niucube.playersdk

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.niucube.comproom.IAudiencePlayerView
import com.niucube.comproom.RoomEntity
import com.pili.pldroid.player.AVOptions
import com.pili.pldroid.player.PLMediaPlayer
import com.pili.pldroid.player.PLOnErrorListener
import com.pili.pldroid.player.PLOnPreparedListener

class QNAudienceAudioPlayer(context: Context) : PLMediaPlayer(context, AVOptions().apply {
    setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
    setInteger(AVOptions.KEY_FAST_OPEN, 1);
    setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 5);
    setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
    setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
}), IAudiencePlayerView, LifecycleObserver {

    private val mHandler = object : Handler(Looper.getMainLooper()) {}


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
        override fun onError(p0: Int, p1: Any?): Boolean {
            var deal = false
            Log.d("QNAudienceAudioPlayer", " setOnErrorListener ${p0}")
            mPLOnErrorListeners.forEach {
                if (it.onError(p0,p1)) {
                    deal = true
                }
            }
            return deal
        }

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
        toStart = true
        mCurrentUrl = roomEntity.providePullUri()
        dataSource = (roomEntity.providePullUri())
        prepareAsync()
    }

    /**
     * 停止播放拉流地址
     * 1角色变跟为主播
     * 2用户角色房间离开销毁
     */
    override fun stopAudiencePlay() {
        toStart = false
        stop()
    }

    private fun reStart() {
        dataSource = mCurrentUrl
        prepareAsync()
    }

    init {
        setOnErrorListener(PLOnErrorListenerWrap())
        addOnErrorListener { it,_->
            if (it == -2) {
                if (toStart) {
                    mHandler.postDelayed(reStartRun, 3000)
                }
            }
            false
        }
        setOnPreparedListener(object : PLOnPreparedListener {
            override fun onPrepared(p0: Int) {
                start()
                Log.d("QNAudienceAudioPlayer","onPrepared")
            }
        })
        setOnInfoListener { i, i2 ,i3->
            Log.d("QNAudienceAudioPlayer","setOnInfoListener"+i+"  "+i2 )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        stop()
        release()
    }

}