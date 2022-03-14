package com.niucube.rtcroom.customtrack

import com.niucube.absroom.*
import com.niucube.rtcroom.RtcRoom

import com.qiniu.droid.rtc.*

class CustomTrackShareManagerImp(val rtcRoom: RtcRoom) : CustomTrackShareManager {
    override fun getUserExtraTrackInfo(tag: String, uid: String): QNTrack? {
        TODO("Not yet implemented")
    }

    override fun pubCustomVideoTrack(trackTag: String, params: VideoTrackParams): VideoChanel {
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
//
//    private val mAllCustomTrack = ArrayList<QNTrack>()
//    private val mUserExtraWindowMap = HashMap<String, QNTextureView>()
//    private val mCustomMicSeatListeners = ArrayList<CustomMicSeatListener>()
//    private val mCustomMicSeats = ArrayList<CustomMicSeat>()
//
//    //麦位信令
//    private var mRtcRoomSignaling = RtcRoomSignaling()
//    private fun getUserCustomSeat(uid: String, tag: String): CustomMicSeat? {
//        mCustomMicSeats.forEach {
//            if (uid == it.uid && it.tag == tag) {
//                return it
//            }
//        }
//        return null
//    }
//
//
//    private fun getUserCustomSeat(uid: String): List<CustomMicSeat> {
//        val seats = ArrayList<CustomMicSeat>()
//        mCustomMicSeats.forEach {
//            if (uid == it.uid) {
//                mCustomMicSeats.add(it)
//            }
//        }
//        return seats
//    }
//
//    private var mQNRTCEngineEventListener = object : SimpleQNRTCListener {
//
//        private fun onPublished(p0: String, p1: MutableList<QNTrackInfo>, isRemote: Boolean) {
//            p1.forEach { track ->
//                when (track.tag) {
//                    RtcRoom.TAG_AUDIO -> {
//                    }
//                    RtcRoom.TAG_CAMERA -> {
//                    }
//                    RtcRoom.TAG_SCREEN -> {
//                    }
//
//                    else -> {
//                        if (isRemote) {
//                            mAllCustomTrack.add(track)
//                        }
//                        mUserExtraWindowMap["${p0}${track.tag}"]?.let {
//                            rtcRoom.mEngine.setRenderTextureWindow(track, it)
//                        }
//                        mUserExtraWindowMap.remove("${p0}${track.tag}")
//                        val seat = CustomMicSeat().apply {
//                            uid = p0
//
//                            if (track.isAudio) {
//                                isVideoOpen = true
//                            }
//                            if (track.isAudio) {
//                                isAudioOpen = true
//                            }
//                        }
//                        mCustomMicSeats.add(seat)
//                        mCustomMicSeatListeners.forEach {
//                            it.onCustomMicSeatAdd(seat)
//                        }
//                        mRtcRoomSignaling.onCustomMicSeatAdd(seat)
//                    }
//                }
//            }
//
//        }
//
//        public override fun onRemotePublished(p0: String, p1: MutableList<QNTrackInfo>) {
//            super.onRemotePublished(p0, p1)
//            //维护房间里所有轨道
//            onPublished(p0, p1, true)
//        }
//
//        //发布本地轨道成功
//        override fun onLocalPublished(p0: MutableList<QNTrackInfo>) {
//            super.onLocalPublished(p0)
//            //维护房间里所有轨道
//            onPublished(
//                com.niucube.comproom.RoomManager.mCurrentRoom?.provideMeId() ?: "",
//                p0,
//                false
//            )
//        }
//
//        override fun onRemoteUnpublished(p0: String, p1: MutableList<QNTrackInfo>) {
//            super.onRemoteUnpublished(p0, p1)
//            //维护房间里所有轨道
//            p1.forEach {
//                if (mAllCustomTrack.contains(it)) {
//                    mAllCustomTrack.remove(it)
//                }
//            }
//            p1.forEach {
//                when (it.tag) {
//                    RtcRoom.TAG_CAMERA -> {
//                    }
//                    RtcRoom.TAG_AUDIO -> {
//                    }
//                    RtcRoom.TAG_SCREEN -> {
//                    }
//                    else -> {
//                        getUserCustomSeat(p0, it.tag)?.let { seat ->
//                            mCustomMicSeats.remove(seat)
//                            mCustomMicSeatListeners.forEach {
//                                it.onCustomMicSeatRemove(seat)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        override fun onRoomLeft() {
//            super.onRoomLeft()
//
//            getUserCustomSeat(
//                com.niucube.comproom.RoomManager.mCurrentRoom?.provideRoomId() ?: ""
//            ).let { seats ->
//                seats.forEach { seat ->
//                    mCustomMicSeats.remove(seat)
//                    mCustomMicSeatListeners.forEach {
//                        it.onCustomMicSeatRemove(seat)
//                    }
//                }
//            }
//            val toRemove = ArrayList<QNTrackInfo>()
//            mAllCustomTrack.forEach {
//                if (it.userId == com.niucube.comproom.RoomManager.mCurrentRoom?.provideMeId() ?: "") {
//                    toRemove.add(it)
//                }
//            }
//            mAllCustomTrack.removeAll(toRemove)
//        }
//
//        override fun onRemoteUserLeft(p0: String) {
//            super.onRemoteUserLeft(p0)
//            getUserCustomSeat(p0).let { seats ->
//                seats.forEach { seat ->
//                    mCustomMicSeats.remove(seat)
//                    mCustomMicSeatListeners.forEach {
//                        it.onCustomMicSeatRemove(seat)
//                    }
//                }
//            }
//            val toRemove = ArrayList<QNTrackInfo>()
//            mAllCustomTrack.forEach {
//                if (it.userId == p0) {
//                    toRemove.add(it)
//                }
//            }
//            mAllCustomTrack.removeAll(toRemove)
//        }
//    }
//    private val mRtmMsgListener = object : RtmMsgListener {
//
//        fun onNewMsgSignaling(msg: String, peerId: String): Boolean {
//            when (msg.optAction()) {
//                action_rtc_pubCustomTrack -> {
//                    if (rtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
//                        val seat = JsonUtils.parseObject(msg.optData(), CustomMicSeat::class.java)
//                            ?: return true
//                        mCustomMicSeats.add(seat)
//                        mCustomMicSeatListeners.forEach {
//                            it.onCustomMicSeatAdd(seat)
//                        }
//                    }
//                    return true
//                }
//
//                action_rtc_unPubCustomTrack -> {
//                    if (rtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
//                        val seat = JsonUtils.parseObject(msg.optData(), CustomMicSeat::class.java)
//                            ?: return true
//                        getUserCustomSeat(seat.uid, seat.tag)?.let { seat ->
//                            mCustomMicSeats.remove(seat)
//                            mCustomMicSeatListeners.forEach {
//                                it.onCustomMicSeatRemove(seat)
//                            }
//                        }
//                    }
//                    return true
//                }
//            }
//            return false
//        }
//
//        /**
//         * 收到消息
//         */
//        override fun onNewMsg(msg: String, peerId: String): Boolean {
//            if (peerId != RoomManager.mCurrentRoom?.provideRoomId()) {
//                return false
//            }
//            return onNewMsgSignaling(msg, peerId)
//        }
//    }
//
//
//    init {
//        rtcRoom.addExtraQNRTCEngineEventListener(mQNRTCEngineEventListener)
//        RtmManager.addRtmChannelListener(mRtmMsgListener)
//        RoomManager.addRoomLifecycleMonitor(object : RoomLifecycleMonitor {
//            override fun onRoomClosed(roomEntity: RoomEntity?) {
//                super.onRoomClosed(roomEntity)
//                RtmManager.removeRtmChannelListener(mRtmMsgListener)
//            }
//        })
//    }
//
//
//    override fun getUserExtraTrackInfo(tag: String, uid: String): QNTrackInfo? {
//        mAllCustomTrack.forEach {
//            if (it.tag == tag && it.userId == uid) {
//                return it
//            }
//        }
//        return null
//    }
//
//    override fun pubCustomVideoTrack(trackTag: String, params: VideoTrackParams): VideoChanel {
//        val ef =
//            QNVideoFormat(params.width, params.height, params.fps)
//        //默认声音轨道创建
//        val track = rtcRoom.mEngine.createTrackInfoBuilder()
//            .setVideoPreviewFormat(ef)
//            .setBitrate((params.bitrate).toInt())
//            .setSourceType(QNSourceType.VIDEO_EXTERNAL)
//            .setMaster(params.isMaster)
//            .setTag(trackTag).create()
//        rtcRoom.mEngine.publishTracks(listOf(track))
//        mAllCustomTrack.add(track)
//        val seat = CustomMicSeat().apply {
//            uid = com.niucube.comproom.RoomManager.mCurrentRoom?.provideMeId()?:""
//            isVideoOpen = true
//        }
//        mRtcRoomSignaling.onCustomMicSeatAdd(seat)
//
//        return object : VideoChanel {
//            override fun sendVideoFrame(videoData: ByteArray, width: Int, height: Int) {
//
//            }
//        }
//    }
//
//    override fun pubCustomAudioTrack(trackTag: String, params: AudioTrackParams) {
//
//    }
//
//    override fun unPubCustomTrack(trackTag: String) {
//        mAllCustomTrack.forEach {
//            if (it.userId == com.niucube.comproom.RoomManager.mCurrentRoom?.provideMeId()
//                && it.tag == trackTag
//            ) {
//                rtcRoom.mEngine.unPublishTracks(listOf(it))
//                mAllCustomTrack.remove(it)
//                return@forEach
//            }
//        }
//        getUserCustomSeat(
//            com.niucube.comproom.RoomManager.mCurrentRoom?.provideMeId() ?: "",
//            trackTag
//        )?.let { seat ->
//            mCustomMicSeats.remove(seat)
//            mCustomMicSeatListeners.forEach {
//                it.onCustomMicSeatRemove(seat)
//            }
//            mRtcRoomSignaling.onCustomMicSeatRemove(seat)
//        }
//    }
//
//
//    //添加定义轨道事件
//    override fun addCustomMicSeatListener(listener: CustomMicSeatListener) {
//        mCustomMicSeatListeners.add(listener)
//    }
//
//    override fun removeCustomMicSeatListener(listener: CustomMicSeatListener) {
//        mCustomMicSeatListeners.remove(listener)
//    }
//
//    /**
//     * 设置某人的自定义轨道窗口 可以在任何时候调用
//     * @param trackTag 自定义轨道的标记
//     * @param uid 用户id
//     */
//    override fun setUserCustomVideoPreview(trackTag: String, uid: String, view: QNTextureView) {
//        var findTrack = false
//        mAllCustomTrack.forEach {
//            if (it.tag == trackTag && it.userId == uid) {
//                findTrack = true
//                rtcRoom.mEngine.setRenderTextureWindow(it, view)
//                return@forEach
//            }
//        }
//        if (!findTrack) {
//            mUserExtraWindowMap["${uid}${trackTag}"] = view
//        }
//    }


}