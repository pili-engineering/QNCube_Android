package com.niucube.qrtcroom.screencapture

import androidx.fragment.app.FragmentActivity
import com.niucube.comproom.*
import com.niucube.qrtcroom.rtc.RtcRoom.Companion.TAG_SCREEN
import com.niucube.absroom.RtcRoomSignaling
import com.niucube.absroom.action_rtc_pubScreen
import com.niucube.absroom.action_rtc_unPubScreen
import com.niucube.absroom.seat.ScreenMicSeat
import com.niucube.qrtcroom.rtc.*
import com.niucube.rtm.RtmManager
import com.niucube.rtm.RtmMsgListener
import com.niucube.rtm.optAction
import com.niucube.rtm.optData
import com.qiniu.droid.rtc.*
import com.qiniu.jsonutil.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ScreenShareManager(val rtcRoom: RtcRoom) {
    //麦位信令
    private val mRtcRoomSignaling by lazy { RtcRoomSignaling(rtcRoom) }
    private val mScreenMicSeatListeners = ArrayList<ScreenMicSeatListener>()
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
        override fun onNewMsg(msg: String, fromId: String, toId: String): Boolean {
            if (toId != rtcRoom.mRTCUserStore.joinRoomParams.groupID) {
                return false
            }
            return onNewMsgSignaling(msg, toId)
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
                        TAG_SCREEN -> {
                            val seat = ScreenMicSeat().apply {
                                uid = p0
                                isVideoOpen = true
                            }
                            mScreenMicSeats.add(seat)
                            mScreenMicSeatListeners.forEach {
                                it.onScreenMicSeatAdd(seat)
                            }
                            if (p0 == rtcRoom.mRTCUserStore.joinRoomParams.meId) {
                                mRtcRoomSignaling.onScreenMicSeatAdd(seat)
                            }
                        }
                    }
                }
            }

            override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserUnpublished(p0, p1)
                p1.forEach {
                    when (it.tag) {
                        TAG_SCREEN -> {
                            getUserScreenSeat(p0)?.let { seat ->
                                mScreenMicSeats.remove(seat)
                                mScreenMicSeatListeners.forEach {
                                    it.onScreenMicSeatRemove(seat)
                                }
                                if (p0 == rtcRoom.mRTCUserStore.joinRoomParams.meId) {
                                    mRtcRoomSignaling.onScreenMicSeatRemove(seat)
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
                        TAG_SCREEN -> {
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
            }
        })
        RtmManager.addRtmChannelListener(mRtmMsgListener)
        rtcRoom.mRTCUserStore.closeCallDispatcher.addCloseObserver(object :
            QRTCUserStore.CloseObserver {
            override fun close() {
                RtmManager.removeRtmChannelListener(mRtmMsgListener)
            }
        })
    }

    fun addScreenMicSeatListener(listener: ScreenMicSeatListener) {
        mScreenMicSeatListeners.add(listener)
    }

    fun removeScreenMicSeatListener(listener: ScreenMicSeatListener) {
        mScreenMicSeatListeners.remove(listener)
    }

    fun setUserScreenWindowView(uid: String, view: QNTextureView) {
        rtcRoom.mRTCUserStore.setUserScreenPreView(uid, view)
    }

    fun getUserScreenTrackInfo(uid: String): QNTrack? {
        return rtcRoom.mRTCUserStore.findUser(uid)?.screenTrack?.track
    }

    private var mScreenTrack: QNScreenVideoTrack? = null
    private fun createScreenTrack(params: QScreenTrackParams): QNScreenVideoTrack {
        val screenEncodeFormat =
            QNScreenVideoTrackConfig(params.tag)
                .apply {
                    videoEncoderConfig = QNVideoEncoderConfig(
                        params.width, params.height, params.fps,
                        params.bitrate.toInt()
                    )
                }
        //默认声音轨道创建
        return QNRTC.createScreenVideoTrack(screenEncodeFormat)
    }

    private var isLocalScreenTrackPub = false

    /**
     * 发布屏幕共享
     */
    private suspend fun pubLocalScreenTrack(params: QScreenTrackParams) =
        suspendCoroutine<Unit> { coroutine ->
            if (mScreenTrack == null) {
                mScreenTrack = createScreenTrack(params)
            }
            rtcRoom.rtcClient.publish(object : QNPublishResultCallback {
                override fun onPublished() {
                    isLocalScreenTrackPub = true
                    rtcRoom.rtcEventWrap.onLocalPublished(
                        rtcRoom.mRTCUserStore.joinRoomParams.meId,
                        listOf(mScreenTrack!!)
                    )
                    coroutine.resume(Unit)
                }

                override fun onError(p0: Int, p1: String) {
                    coroutine.resumeWithException(RtcException(p0, p1))
                }
            }, listOf(mScreenTrack))
        }

    /**
     * 请求屏幕共享权限并发布屏幕共享
     */
    fun pubLocalScreenTrackWithPermissionCheck(
        activity: FragmentActivity,
        callback: ScreenCapturePlugin.ScreenCaptureListener,
        params: QScreenTrackParams
    ) {
        ScreenCapturePlugin.getInstance().startMediaRecorder(activity,
            object : ScreenCapturePlugin.ScreenCaptureListener {
                override fun onSuccess() {
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            pubLocalScreenTrack(params)
                            callback.onSuccess()
                        } catch (e: RtcException) {
                            callback.onError(e.code, e.msg)
                        }
                    }
                }

                override fun onError(code: Int, msg: String?) {
                    callback.onError(code, msg)
                }
            })
    }

    fun unPubLocalScreenTrack() {
        if (mScreenTrack == null) {
            return
        }
        rtcRoom.rtcClient.unpublish(listOf(mScreenTrack))
        rtcRoom.rtcEventWrap.onLocalUnpublished(
            rtcRoom.mRTCUserStore.joinRoomParams.meId,
            listOf(mScreenTrack!!)
        )
        isLocalScreenTrackPub = false
    }
}