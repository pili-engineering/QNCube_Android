package com.niucube.basemutableroom.adminTrack

import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.niucube.basemutableroom.RtcRoom
import com.qiniu.droid.rtc.*

class AdminTrackManager(val rtcRoom: RtcRoom) {

    private var mAllAdminTrack = ArrayList<QNRemoteTrack>()
    private val mAdminTrackListeners = ArrayList<AdminTrackListener>()
    private val mAdminTrackListener = object : AdminTrackListener {
        override fun onAdminPubAudio(track: QNRemoteTrack): Boolean {
            var isNeed = false
            mAdminTrackListeners.forEach {
                if (it.onAdminPubAudio(track)) {
                    isNeed = true
                }
            }
            return isNeed
        }

        override fun onAdminPubVideo(track: QNRemoteTrack): Boolean {
            var isNeed = false
            mAdminTrackListeners.forEach {
                if (it.onAdminPubVideo(track)) {
                    isNeed = true
                }
            }
            return isNeed
        }

        override fun onAdminUnPubAudio(track: QNRemoteTrack) {
            mAdminTrackListeners.forEach {
                it.onAdminUnPubAudio(track)
            }
        }

        override fun onAdminUnPubVideo(track: QNRemoteTrack) {
            mAdminTrackListeners.forEach {
                it.onAdminUnPubVideo(track)
            }
        }
    }

    init {
        rtcRoom.addExtraQNRTCEngineEventListener(object : SimpleQNRTCListener {
            override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserPublished(p0, p1)
                if (p0.startsWith("admin-publisher")) {
                    mAllAdminTrack.addAll(p1)
                    p1.forEach {
                        if (it is QNRemoteVideoTrack) {
                            if (mAdminTrackListener.onAdminPubVideo(it)) {
                                rtcRoom.mClient.subscribe(it)
                            }
                        }
                        if (it is QNRemoteAudioTrack) {
                            if (mAdminTrackListener.onAdminPubAudio(it)) {
                                rtcRoom.mClient.subscribe(it)
                            }
                        }
                    }
                    rtcRoom.mClient.unsubscribe(p1)
                }
            }

            override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserUnpublished(p0, p1)
                p1.forEach {
                    if (it is QNRemoteVideoTrack) {
                        mAdminTrackListener.onAdminUnPubVideo(it)
                    }
                    if (it is QNRemoteAudioTrack) {
                        mAdminTrackListener.onAdminUnPubAudio(it)
                    }
                    if (mAllAdminTrack.contains(it)) {
                        mAllAdminTrack.remove(it)
                    }
                }
            }
        })
    }

    fun addAdminTrackListener(listener: AdminTrackListener) {
        mAdminTrackListeners.add(listener)
    }

    fun removeAdminTrackListener(listener: AdminTrackListener) {
        mAdminTrackListeners.remove(listener)
    }

    interface AdminTrackListener {
        fun onAdminPubVideo(track: QNRemoteTrack): Boolean {
            return false
        }

        fun onAdminPubAudio(track: QNRemoteTrack): Boolean {
            return false
        }

        fun onAdminUnPubVideo(track: QNRemoteTrack) {}
        fun onAdminUnPubAudio(track: QNRemoteTrack) {}
    }

}