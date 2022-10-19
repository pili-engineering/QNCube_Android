package com.niucube.qrtcroom.rtc

import com.niucube.qrtcroom.liblog.QLiveLogUtil
import com.niucube.qrtcroom.rtc.RtcRoom.Companion.TAG_CAMERA
import com.niucube.qrtcroom.rtc.RtcRoom.Companion.TAG_SCREEN
import com.qiniu.droid.rtc.*
import java.util.*
import kotlin.collections.ArrayList

class QRTCUserStore {
    var joinRoomParams = QJoinRoomParams("", "", "", "")
    var localVideoTrack: QNCameraVideoTrack? = null
    var localAudioTrack: QNMicrophoneAudioTrack? = null
    val rtcUsers = LinkedList<QRTCUser>()
    private var localCameraPreview: QNRenderView? = null

    fun addUser(uid: String) {
        val user = QRTCUser().apply {
            this.uid = uid
        }
        if (findUser(uid) == null) {
            QLiveLogUtil.d("QRTCUserStore", "addUser ${user.uid} ")
            rtcUsers.add(user)
        }
        if (user.uid == joinRoomParams.meId) {
            user.cameraTrack.preView = localCameraPreview
            user.cameraTrack.track = localVideoTrack
            user.cameraTrack.track = localVideoTrack
        }
    }

    fun clearAllTrack() {
        rtcUsers.forEach {
            it.cameraTrack.track = null
            it.cameraTrack.mergeOptionWithOutTrackID = null
            it.microphoneTrack.track = null
            it.microphoneTrack.mergeOptionWithOutTrackID = null
            it.screenTrack.track = null
            it.screenTrack.mergeOptionWithOutTrackID = null
        }
    }

    fun clear(destroy: Boolean = true) {

        QLiveLogUtil.d("QRTCUserStore", "clear  ")
        rtcUsers.clear()
        if (destroy) {
            localAudioTrack?.destroy()
            localVideoTrack?.destroy()
        }
        localAudioTrack = null
        localVideoTrack = null
    }

    fun clearUser(uid: String) {
        findUser(uid)?.let {
            rtcUsers.remove(it)
        }
    }

    fun removeUserTrack(uid: String, track: QNTrack) {
        val user = findUser(uid) ?: return
        QLiveLogUtil.d("QRTCUserStore", "removeUserTrack ${uid}  ")
        when (track) {
            is QNLocalVideoTrack -> {
                if (track.tag == TAG_CAMERA) {
                    user.cameraTrack.track = null
                    user.cameraTrack.preView = null
                }
                if (track.tag == TAG_SCREEN) {
                    user.screenTrack.track = null
                    user.screenTrack.preView = null
                }
            }

            is QNMicrophoneAudioTrack -> {
                user.microphoneTrack.track = null
            }

            is QNRemoteVideoTrack -> {
                if (track.tag == TAG_CAMERA) {
                    user.cameraTrack.track = null
                    user.cameraTrack.preView = null
                }
                if (track.tag == TAG_SCREEN) {
                    user.screenTrack.track = null
                    user.screenTrack.preView = null
                }
            }
            is QNRemoteAudioTrack -> {
                user.microphoneTrack.track = null
            }
        }
    }

    fun setUserTrack(uid: String, track: QNTrack) {
        QLiveLogUtil.d("QRTCUserStore", "setUserTrack ${uid} ${track.trackID} ")
        val user = findUser(uid) ?: return
        when (track) {
            is QNLocalVideoTrack -> {
                if (track.tag == TAG_CAMERA) {
                    user.cameraTrack.track = track
                    if (!isPlayLocal) {
                        user.cameraTrack.preView?.let {
                            isPlayLocal = true
                            track.play(it)
                        }
                    }
                }
                if (track.tag == TAG_SCREEN) {
                    user.screenTrack.track = track
                    user.screenTrack.preView?.let {
                        track.play(it)
                    }
                }
            }
            is QNMicrophoneAudioTrack -> {
                user.microphoneTrack.track = track
            }
            is QNRemoteVideoTrack -> {

                if (track.tag == TAG_SCREEN) {
                    user.screenTrack.track = track
                    user.screenTrack.preView?.let {
                        track.play(it)
                    }
                }else{
                    user.cameraTrack.track = track
                    user.cameraTrack.preView?.let {
                        track.play(it)
                    }
                }
            }

            is QNRemoteAudioTrack -> {
                user.microphoneTrack.track = track
            }
        }
    }

    fun setUserCameraPreView(uid: String, preView: QNRenderView) {
        QLiveLogUtil.d("QRTCUserStore", "setUserCameraPreView ${uid} ")
        val user = findUser(uid) ?: return
        user.cameraTrack.preView = preView
        user.cameraTrack.track?.tryPlay(preView)
    }

    fun setUserScreenPreView(uid: String, preView: QNRenderView) {
        val user = findUser(uid) ?: return
        user.screenTrack.preView = preView
        user.screenTrack.track?.tryPlay(preView)
    }

    private var isPlayLocal = false
    fun setLocalCameraPreView(preView: QNRenderView) {
        val user = findUser(joinRoomParams.meId)
        if (user?.cameraTrack?.track != null) {
            isPlayLocal = true
            user.cameraTrack.preView = preView
            user.cameraTrack.track?.let {
                isPlayLocal = true
                it.tryPlay(preView)
            }
        } else {
            localCameraPreview = preView
            localVideoTrack?.let {
                isPlayLocal = true
                it.play(preView)
            }
        }
    }

    fun findUser(uid: String): QRTCUser? {
        val target: QRTCUser? = rtcUsers.find {
            it.uid == uid
        }
        return target
    }

    fun clearTrackMergeOption() {
        QLiveLogUtil.d("QRTCUserStore", "clearTrackMergeOption ")
        rtcUsers.forEach {
            it.cameraTrack.mergeOptionWithOutTrackID = null
            it.microphoneTrack.mergeOptionWithOutTrackID = null
        }
    }

    class QRTCUser {
        var uid: String = ""
        var userExt: Any = ""
        var userData: String = ""
        var cameraTrack = QRTCVideoTrack()
        var microphoneTrack = QRTCMicrophoneTrack()
        var screenTrack = QRTCVideoTrack()

        fun setPreviewVisibility(visibility: Int) {
            val previews = ArrayList<QNRenderView>()
            cameraTrack.preView?.let {
                previews.add(it)
            }
            screenTrack.preView?.let {
                previews.add(it)
            }
            previews.forEach {
                if (it is QNTextureView) {
                    (it as QNTextureView?)?.visibility = visibility
                }
                if (it is QNTextureView) {
                    (it as QNTextureView?)?.visibility = visibility
                }
            }
        }
    }

    class QRTCVideoTrack {
        var track: QNTrack? = null
        var mergeOptionWithOutTrackID: QNTranscodingLiveStreamingTrack? = null
        var preView: QNRenderView? = null
    }


    class QRTCMicrophoneTrack {
        var track: QNTrack? = null
        var mergeOptionWithOutTrackID: QNTranscodingLiveStreamingTrack? = null
    }

    interface CloseObserver {
        fun close()
    }

    internal val closeCallDispatcher = CloseCallDispatcher()

    class CloseCallDispatcher : CloseObserver {
        private val closeCalls = LinkedList<CloseObserver>()
        fun addCloseObserver(observer: CloseObserver) {
            closeCalls.add(observer)
        }

        override fun close() {
            closeCalls.forEach {
                it.close()
            }
            closeCalls.clear()
        }
    }

    fun QNTrack.tryPlay(var1: QNRenderView) {
        if (this is QNLocalVideoTrack) {
            play(var1)
            return
        }
        if (this is QNRemoteVideoTrack) {
            play(var1)
            return
        }
    }
}