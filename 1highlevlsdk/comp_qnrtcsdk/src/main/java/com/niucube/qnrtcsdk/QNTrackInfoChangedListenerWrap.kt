package com.niucube.qnrtcsdk

import com.qiniu.droid.rtc.QNTrackInfoChangedListener
import com.qiniu.droid.rtc.QNTrackProfile

class QNTrackInfoChangedListenerWrap : QNTrackInfoChangedListener {

    private val mQNTrackInfoChangedListener = ArrayList<QNTrackInfoChangedListener>()

    fun add(trackInfoChangedListener:QNTrackInfoChangedListener) {
        mQNTrackInfoChangedListener.add(trackInfoChangedListener)
    }
    fun remove(trackInfoChangedListener:QNTrackInfoChangedListener) {
        mQNTrackInfoChangedListener.remove(trackInfoChangedListener)
    }

    override fun onMuteStateChanged(isMuted: Boolean) {
        super.onMuteStateChanged(isMuted)
        mQNTrackInfoChangedListener.forEach {
            it.onMuteStateChanged(isMuted)

        }
    }

    override fun onVideoProfileChanged(profile: QNTrackProfile?) {
        super.onVideoProfileChanged(profile)
        mQNTrackInfoChangedListener.forEach {
            it.onVideoProfileChanged(profile)
        }
    }
}