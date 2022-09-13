package com.niucube.qrtcroom.rtc

import com.niucube.qrtcroom.qplayer.QPlayerEventListener


class QPlayerEventListenerWarp: QPlayerEventListener {

    private val mQPlayerEventListeners = ArrayList<QPlayerEventListener>()
    fun addEventListener(listener: QPlayerEventListener){
        mQPlayerEventListeners.add(listener)
    }

    fun removeEventListener(listener: QPlayerEventListener){
        mQPlayerEventListeners.remove(listener)
    }
    fun clear(){
        mQPlayerEventListeners.clear()
    }

    override fun onPrepared(preparedTime: Int) {
        mQPlayerEventListeners.forEach {
            it.onPrepared(preparedTime)
        }
    }

    override fun onInfo(what: Int, extra: Int) {
        mQPlayerEventListeners.forEach {
            it.onInfo(what, extra)
        }
    }

    override fun onBufferingUpdate(percent: Int) {
        mQPlayerEventListeners.forEach {
            it.onBufferingUpdate(percent)
        }
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        mQPlayerEventListeners.forEach {
            it.onVideoSizeChanged(width, height)
        }
    }

    override fun onError(errorCode: Int): Boolean {
        var deal = false
        mQPlayerEventListeners.forEach {
            if(it.onError(errorCode)){
                deal = true
            }
        }
        return deal
    }
}