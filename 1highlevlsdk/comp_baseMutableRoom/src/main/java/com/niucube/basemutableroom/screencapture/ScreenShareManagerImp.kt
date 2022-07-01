package com.niucube.basemutableroom.screencapture

import androidx.fragment.app.FragmentActivity
import com.niucube.comproom.*
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.niucube.basemutableroom.RtcRoom
import com.niucube.basemutableroom.RtcRoom.Companion.TAG_SCREEN
import com.niucube.basemutableroom.absroom.RtcRoomSignaling
import com.niucube.basemutableroom.absroom.ScreenTrackParams
import com.niucube.basemutableroom.absroom.action_rtc_pubScreen
import com.niucube.basemutableroom.absroom.action_rtc_unPubScreen
import com.niucube.basemutableroom.absroom.seat.ScreenMicSeat
import com.niucube.basemutableroom.tryPlay
import com.niucube.rtm.RtmManager
import com.niucube.rtm.RtmMsgListener
import com.niucube.rtm.optAction
import com.niucube.rtm.optData
import com.qiniu.droid.rtc.*
import com.qiniu.jsonutil.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ScreenShareManagerImp(val rtcRoom: RtcRoom) : ScreenShareManager {
    //麦位信令
    private var mRtcRoomSignaling = RtcRoomSignaling()
    private val mUserScreenWindowMap = HashMap<String, QNTextureView>()
    private val mScreenMicSeatListeners = ArrayList<ScreenMicSeatListener>()

    //房间里存在的所以轨道
    private var mAllScreenTrack = ArrayList<QNTrack>()

    private val mScreenMicSeats = ArrayList<ScreenMicSeat>()
    private fun getUserScreenSeat(uid: String): ScreenMicSeat? {
        mScreenMicSeats.forEach {
            if (uid == it.uid) {
                return it
            }
        }
        return null
    }


    private val mRtmMsgListener = object : RtmMsgListener {

        fun onNewMsgSignaling(msg: String, peerId: String): Boolean {

            when (msg.optAction()) {
                action_rtc_unPubScreen -> {
                    if (rtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
                        val seatTemp =
                            JsonUtils.parseObject(msg.optData(), ScreenMicSeat::class.java)
                                ?: return true
                        getUserScreenSeat(
                            seatTemp.uid
                        )?.let { seat ->
                            seat.isVideoOpen = false
                            mScreenMicSeats.remove(seat)
                            mScreenMicSeatListeners.forEach {
                                it.onScreenMicSeatRemove(seat)
                            }
                        }
                    }
                    return true
                }
                action_rtc_pubScreen -> {
                    if (rtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
                        val seat = JsonUtils.parseObject(msg.optData(), ScreenMicSeat::class.java)
                            ?: return true
                        mScreenMicSeats.add(seat)
                        mScreenMicSeatListeners.forEach {
                            it.onScreenMicSeatAdd(seat)
                        }
                    }
                    return true
                }
            }
            return false
        }

        /**
         * 收到消息
         */
        override fun onNewMsg(msg: String, fromId: String, peerId: String): Boolean {
            if (peerId != RoomManager.mCurrentRoom?.provideRoomId()) {
                return false
            }
            return onNewMsgSignaling(msg, peerId)
        }
    }


    init {
        rtcRoom.addExtraQNRTCEngineEventListener(object : SimpleQNRTCListener {

            override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
                super.onLocalPublished(var1, var2)
                onPublished(var1, var2, false)
            }


            override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserPublished(p0, p1)
                onPublished(p0, p1, true)
            }

            private fun onPublished(p0: String, p1: List<QNTrack>, isRemote: Boolean) {
                p1.forEach { track ->
                    when (track.tag) {

                        //提前设置了这个用户的屏幕共享预览窗口 现在把他绑定
                        RtcRoom.TAG_SCREEN -> {
                            if (isRemote) {
                                if(isRemote){
                                    rtcRoom. mClient.subscribe(track as QNRemoteTrack)
                                }
                                mAllScreenTrack.add(track)
                            }
                            mUserScreenWindowMap[p0]?.let {
                                (track ).tryPlay(it)
                            }
                            mUserScreenWindowMap.remove(p0)

                            val seat = ScreenMicSeat().apply {
                                uid = p0
                                isVideoOpen = true
                            }
                            mScreenMicSeats.add(seat)
                            mScreenMicSeatListeners.forEach {
                                it.onScreenMicSeatAdd(seat)
                            }
                        }
                    }
                }
            }

            override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserUnpublished(p0, p1)
                p1.forEach {
                    when (it.tag) {
                        RtcRoom.TAG_SCREEN -> {
                            mAllScreenTrack.remove(it)
                            getUserScreenSeat(p0)?.let { seat ->
                                mScreenMicSeats.remove(seat)
                                mScreenMicSeatListeners.forEach {
                                    it.onScreenMicSeatRemove(seat)
                                }
                            }
                        }
                    }
                }
            }

            override fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>) {
                super.onLocalUnpublished(var1, var2)
                var2.forEach {
                    when (it.tag) {
                        RtcRoom.TAG_SCREEN -> {
                            mAllScreenTrack.remove(it)
                            getUserScreenSeat(var1)?.let { seat ->
                                mScreenMicSeats.remove(seat)
                                mScreenMicSeatListeners.forEach {
                                    it.onScreenMicSeatRemove(seat)
                                }
                            }
                        }
                    }
                }
            }

            override fun onUserLeft(p0: String) {
                super.onUserLeft(p0)
                getUserScreenSeat(
                    p0
                )?.let { seat ->
                    mScreenMicSeats.remove(seat)
                    mScreenMicSeatListeners.forEach {
                        it.onScreenMicSeatRemove(seat)
                    }
                }
                val toRm = ArrayList<QNTrack>()
                mAllScreenTrack.forEach {
                    if(it.userID == p0){
                        toRm.add(it)
                    }
                }
                mAllScreenTrack.removeAll(toRm)
            }
        })

        RtmManager.addRtmChannelListener(mRtmMsgListener)
        RoomManager.addRoomLifecycleMonitor(object : RoomLifecycleMonitor {
            override fun onRoomClosed(roomEntity: RoomEntity?) {
                super.onRoomClosed(roomEntity)
                RtmManager.removeRtmChannelListener(mRtmMsgListener)
            }
        })
    }

    private var mScreenTrack: QNScreenVideoTrack? = null
    private fun createScreenTrack(params: ScreenTrackParams): QNScreenVideoTrack {
        val screenEncodeFormat =
            QNScreenVideoTrackConfig(params.tag)
                .apply {
                    setVideoEncoderConfig(
                        QNVideoEncoderConfig(
                            params.width, params.height, params.fps,
                            params.bitrate.toInt()
                        )
                    )

                }
        //默认声音轨道创建
        return QNRTC.createScreenVideoTrack(screenEncodeFormat)
    }

    private var isLocalScreenTrackPub = false

    /**
     * 发布屏幕共享
     */
    override fun pubLocalScreenTrack(params: ScreenTrackParams) {
        if (mScreenTrack == null) {
            mScreenTrack = createScreenTrack(params)
        }
        rtcRoom.mClient.publish(object : QNPublishResultCallback {
            override fun onPublished() {
                isLocalScreenTrackPub = true
                rtcRoom.mQNRTCEngineEventWrap.onLocalPublished(RoomManager.mCurrentRoom?.provideMeId()?:"",
                    listOf(mScreenTrack!!))
            }

            override fun onError(p0: Int, p1: String?) {

            }
        },listOf(mScreenTrack))

    }

    /**
     * 请求屏幕共享权限并发布屏幕共享
     */
    override fun pubLocalScreenTrackWithPermissionCheck(
        activity: FragmentActivity,
        callback: ScreenCapturePlugin.ScreenCaptureListener,
        params: ScreenTrackParams
    ) {
        ScreenCapturePlugin.getInstance().startMediaRecorder(activity,
            object : ScreenCapturePlugin.ScreenCaptureListener {
                override fun onSuccess() {
                    GlobalScope.launch(Dispatchers.Main) {
                        pubLocalScreenTrack(params)
                    }
                    callback.onSuccess()
                }

                override fun onError(code: Int, msg: String?) {
                    callback.onError(code, msg)
                }
            })
    }


    override fun unPubLocalScreenTrack() {
        if (mScreenTrack == null) {
            return
        }
        rtcRoom.mClient.unpublish(listOf(mScreenTrack))
        rtcRoom.mQNRTCEngineEventWrap.onLocalUnpublished(RoomManager.mCurrentRoom?.provideMeId()?:"",
            listOf(mScreenTrack!!))
        isLocalScreenTrackPub = false
    }


    override fun addScreenMicSeatListener(listener: ScreenMicSeatListener) {
        mScreenMicSeatListeners.add(listener)
    }

    override fun removeScreenMicSeatListener(listener: ScreenMicSeatListener) {
        mScreenMicSeatListeners.remove(listener)
    }

    override fun setUserScreenWindowView(uid: String, view: QNTextureView) {
        var findTrack = false
        mAllScreenTrack.forEach {
            if (it.tag == TAG_SCREEN && it.userID == uid) {
                findTrack = true
                ( it ).tryPlay(view)
                return@forEach
            }
        }
        if (!findTrack) {
            mUserScreenWindowMap[uid] = view
        }
    }

    override fun getUserScreenTrackInfo(uid: String): QNTrack? {
        if (uid == com.niucube.comproom.RoomManager.mCurrentRoom?.provideMeId()) {
            return mScreenTrack
        }
        mAllScreenTrack.forEach {
            if (it.tag == TAG_SCREEN && it.userID == uid ) {
                return it
            }
        }
        return null
    }
}