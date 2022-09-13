package com.niucube.qrtcroom.adminTrack

import android.util.Log
import com.niucube.qrtcroom.rtc.QRTCUserStore
import com.niucube.qrtcroom.rtc.SimpleQNRTCListener
import com.niucube.qrtcroom.rtc.RtcRoom
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
            override fun onUserJoined(p0: String, p1: String?) {
                super.onUserJoined(p0, p1)
                if (p0.startsWith("admin-publisher")) {
                    Log.d("AdminTrackManager","admin-publisher")
                    rtcRoom.mRTCUserStore.addUser(p0)
                }
            }
            override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserPublished(p0, p1)
                if (p0.startsWith("admin-publisher")) {
                    mAllAdminTrack.addAll(p1)
                    p1.forEach {
                        rtcRoom.mRTCUserStore.setUserTrack(p0,it)
                        if (it is QNRemoteVideoTrack) {
                            if (mAdminTrackListener.onAdminPubVideo(it)) {
                                rtcRoom.rtcClient.subscribe(it)
                            }
                        }
                        if (it is QNRemoteAudioTrack) {
                            if (mAdminTrackListener.onAdminPubAudio(it)) {
                                rtcRoom.rtcClient.subscribe(it)
                            }
                        }
                    }
                }
            }

            override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserUnpublished(p0, p1)
                if (p0.startsWith("admin-publisher")) {
                    p1.forEach {
                        rtcRoom.mRTCUserStore.removeUserTrack(p0,it)
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