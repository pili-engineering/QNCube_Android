package com.niucube.module.videowatch.core


import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitMutableLiverRoom
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.niucube.qrtcroom.adminTrack.AdminTrackManager
import com.niucube.qrtcroom.mixstream.MixStreamManager
import com.qiniu.droid.rtc.QNRemoteTrack
import com.qiniu.droid.rtc.QNRenderMode
import com.qiniu.droid.rtc.QNTranscodingLiveStreamingConfig
import com.qiniu.droid.rtc.QNTranscodingLiveStreamingTrack
import com.qiniudemo.baseapp.been.hostId

class VideoRoomMixHelper {
    val mMixStreamParams = QNTranscodingLiveStreamingConfig().apply {
        width = 1080
        height = 1080
        bitrate = 1600
        videoFrameRate = 15
    }

    val videoMergeTrackOption = QNTranscodingLiveStreamingTrack().apply {
        x = 0
        y = 0
        zOrder = 0
        width = 1080
        height = 720
        renderMode = QNRenderMode.FILL
    }

    val hostTrackOption = QNTranscodingLiveStreamingTrack().apply {
        x = 0
        y = 720
        zOrder = 0
        width = 360
        height = 360
        renderMode = QNRenderMode.ASPECT_FIT
    }

    val hostTrackOption2 = QNTranscodingLiveStreamingTrack().apply {
        x = 720
        y = 720
        zOrder = 0
        width = 360
        height = 360
        renderMode = QNRenderMode.ASPECT_FIT
    }

    fun start(rtcRoom: LazySitMutableLiverRoom) {
        rtcRoom.mixStreamManager.startMixStreamJob(mMixStreamParams)
    }

    private var lastVideoTrack = ""
    private var lastAudio = ""

    fun attach(rtcRoom: LazySitMutableLiverRoom) {
        rtcRoom.adminTrackManager.addAdminTrackListener(object :
            AdminTrackManager.AdminTrackListener {
            override fun onAdminPubAudio(track: QNRemoteTrack): Boolean {
                if (lastAudio.isNotEmpty()) {
                    rtcRoom.mixStreamManager.updateUserAudioMergeOptions(track.userID, null, true)
                }
                rtcRoom.mixStreamManager.updateUserAudioMergeOptions(
                    track.userID,
                    QNTranscodingLiveStreamingTrack(),
                    false
                )
                lastAudio = track.trackID
                return super.onAdminPubAudio(track)
            }

            override fun onAdminPubVideo(track: QNRemoteTrack): Boolean {
                if (lastVideoTrack.isNotEmpty()) {
                    rtcRoom.mixStreamManager.updateUserVideoMergeOptions(track.userID, null, true)
                }
                rtcRoom.mixStreamManager.updateUserVideoMergeOptions(
                    track.userID,
                    videoMergeTrackOption,
                    true
                )
                lastVideoTrack = track.trackID
                return super.onAdminPubAudio(track)
            }

            override fun onAdminUnPubVideo(track: QNRemoteTrack) {
                super.onAdminUnPubVideo(track)
                rtcRoom.mixStreamManager.updateUserVideoMergeOptions(track.userID, null, false)
            }

            override fun onAdminUnPubAudio(track: QNRemoteTrack) {
                super.onAdminUnPubAudio(track)
                rtcRoom.mixStreamManager.updateUserAudioMergeOptions(track.userID, null, false)
            }

        })
        rtcRoom.addUserMicSeatListener(object : UserMicSeatListener {
            override fun onUserSitDown(micSeat: LazySitUserMicSeat) {
                if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                    rtcRoom.mixStreamManager
                        .updateUserVideoMergeOptions(micSeat.uid, hostTrackOption, true)
                } else {
                    rtcRoom.mixStreamManager
                        .updateUserVideoMergeOptions(micSeat.uid, hostTrackOption2, true)
                }
                rtcRoom.mixStreamManager.updateUserAudioMergeOptions(
                    micSeat.uid,
                    QNTranscodingLiveStreamingTrack(),
                    true
                )
            }

            override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {
                if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                    rtcRoom.mixStreamManager.updateUserVideoMergeOptions(micSeat.uid, null, false)
                } else {
                    rtcRoom.mixStreamManager.updateUserVideoMergeOptions(micSeat.uid, null, false)
                }
                rtcRoom.mixStreamManager.updateUserAudioMergeOptions(micSeat.uid, null, false)
            }

            override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {}
            override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {}
        })
    }

}