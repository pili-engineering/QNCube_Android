package com.niucube.module.videowatch.core


import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitMutableLiverRoom
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.niucube.basemutableroom.adminTrack.AdminTrackManager
import com.niucube.basemutableroom.mixstream.MixStreamManager
import com.qiniu.droid.rtc.QNRemoteTrack
import com.qiniu.droid.rtc.QNRenderMode
import com.qiniudemo.baseapp.been.hostId

class VideoRoomMixHelper {
    val mMixStreamParams = MixStreamManager.MixStreamParams().apply {
        mixStreamWidth = 1080
        mixStringHeight = 1080
        mixBitrate = 3420 * 1000
        fps = 15
    }

    val videoMergeTrackOption = MixStreamManager.MergeTrackOption().apply {
        mX = 0
        mY = 0
        mZ = 0
        mWidth = 1080
        mHeight = 720
        mStretchMode = QNRenderMode.FILL
    }

    val hostTrackOption = MixStreamManager.MergeTrackOption().apply {
        mX = 0
        mY = 720
        mZ = 0
        mWidth = 360
        mHeight = 360
        mStretchMode = QNRenderMode.ASPECT_FIT
    }

    val hostTrackOption2 = MixStreamManager.MergeTrackOption().apply {
        mX = 720
        mY = 720
        mZ = 0
        mWidth = 360
        mHeight = 360
        mStretchMode = QNRenderMode.ASPECT_FIT
    }

    fun start(rtcRoom: LazySitMutableLiverRoom){
        rtcRoom.getMixStreamHelper().startMixStreamJob()
    }

    private var lastVideoTrack = ""
    private var lastAudio=""


    fun attach(rtcRoom: LazySitMutableLiverRoom) {
        rtcRoom.getMixStreamHelper().setMixParams(mMixStreamParams)

        rtcRoom.adminTrackManager.addAdminTrackListener(object :
            AdminTrackManager.AdminTrackListener {
            override fun onAdminPubAudio(track: QNRemoteTrack): Boolean {
                if(lastAudio.isNotEmpty()){
                    rtcRoom.getMixStreamHelper().updateAudioMergeOptions(lastAudio, false)
                }
                rtcRoom.getMixStreamHelper().updateAudioMergeOptions(track.trackID, true)
                lastAudio = track.trackID
                return super.onAdminPubAudio(track)
            }

            override fun onAdminPubVideo(track: QNRemoteTrack): Boolean {
                if(lastVideoTrack.isNotEmpty()){
                    rtcRoom.getMixStreamHelper().updateVideoMergeOptions(lastVideoTrack, null)
                }
                rtcRoom.getMixStreamHelper()
                    .updateVideoMergeOptions(track.trackID, videoMergeTrackOption)
                lastVideoTrack = track.trackID
                return super.onAdminPubAudio(track)
            }

            override fun onAdminUnPubVideo(track: QNRemoteTrack) {
                super.onAdminUnPubVideo(track)
                rtcRoom.getMixStreamHelper()
                    .updateVideoMergeOptions(track.trackID, null)
            }

            override fun onAdminUnPubAudio(track: QNRemoteTrack) {
                super.onAdminUnPubAudio(track)
                rtcRoom.getMixStreamHelper().updateAudioMergeOptions(track.trackID, false)
            }

        })
        rtcRoom.addUserMicSeatListener(object : UserMicSeatListener {

            override fun onUserSitDown(micSeat: LazySitUserMicSeat) {
                if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                    rtcRoom.getMixStreamHelper()
                        .updateUserVideoMergeOptions(micSeat.uid, hostTrackOption)
                } else {
                    rtcRoom.getMixStreamHelper()
                        .updateUserVideoMergeOptions(micSeat.uid, hostTrackOption2)
                }
                rtcRoom.getMixStreamHelper().updateUserAudioMergeOptions(micSeat.uid, true)
            }

            override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {
                if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                    rtcRoom.getMixStreamHelper().updateUserVideoMergeOptions(micSeat.uid, null)
                } else {
                    rtcRoom.getMixStreamHelper().updateUserVideoMergeOptions(micSeat.uid, null)
                }
                rtcRoom.getMixStreamHelper().updateUserAudioMergeOptions(micSeat.uid, false)
            }

            override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {}
            override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {}
        })
    }

}