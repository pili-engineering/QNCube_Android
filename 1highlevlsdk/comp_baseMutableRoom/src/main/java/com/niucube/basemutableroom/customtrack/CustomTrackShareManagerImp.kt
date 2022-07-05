package com.niucube.basemutableroom.customtrack

import com.niucube.basemutableroom.RtcRoom
import com.niucube.basemutableroom.absroom.AudioTrackParams
import com.niucube.basemutableroom.absroom.VideoTrackParams

import com.qiniu.droid.rtc.*

class CustomTrackShareManagerImp(val rtcRoom: RtcRoom) : CustomTrackShareManager {
    override fun getUserExtraTrackInfo(tag: String, uid: String): QNTrack? {
        TODO("Not yet implemented")
    }

    override fun pubCustomVideoTrack(trackTag: String, params: VideoTrackParams): VideoChannel {
        TODO("Not yet implemented")
    }

    override fun pubCustomAudioTrack(trackTag: String, params: AudioTrackParams) {
        TODO("Not yet implemented")
    }

    override fun unPubCustomTrack(trackTag: String) {
        TODO("Not yet implemented")
    }

    override fun addCustomMicSeatListener(listener: CustomMicSeatListener) {
        TODO("Not yet implemented")
    }

    override fun removeCustomMicSeatListener(listener: CustomMicSeatListener) {
        TODO("Not yet implemented")
    }

    override fun setUserCustomVideoPreview(trackTag: String, uid: String, view: QNTextureView) {
        TODO("Not yet implemented")
    }
}