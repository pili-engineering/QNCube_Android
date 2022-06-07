package com.qncube.linkmicservice

import android.util.Log
import com.niucube.rtm.RtmException
import com.niucube.rtm.RtmManager
import com.niucube.rtm.msg.RtmTextMsg
import com.niucube.rtm.sendChannelMsg
import com.nucube.rtclive.DefaultExtQNClientEventListener
import com.nucube.rtclive.QNCameraParams
import com.nucube.rtclive.QNMicrophoneParams
import com.nucube.rtclive.RtcLiveRoom
import com.qiniu.droid.rtc.*
import com.qiniu.jsonutil.JsonUtils
import com.qncube.linkmicservice.QNLinkMicServiceImpl.Companion.liveroom_miclinker_camera_mute
import com.qncube.linkmicservice.QNLinkMicServiceImpl.Companion.liveroom_miclinker_join
import com.qncube.linkmicservice.QNLinkMicServiceImpl.Companion.liveroom_miclinker_left
import com.qncube.linkmicservice.QNLinkMicServiceImpl.Companion.liveroom_miclinker_microphone_mute
import com.qncube.liveroom_pullclient.QNLivePullClient
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.MuteMode
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.UidMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class QNAudienceMicLinkerImpl(val context: MicLinkContext) : QNAudienceMicLinker, BaseService() {

    private val mLinkDateSource = LinkDateSource()

    private val mPlayer: IPullPlayer?
        get() {
            return (client as QNLivePullClient).pullPreview
        }

    private val mLinkMicListeners = ArrayList<QNAudienceMicLinker.LinkMicListener>()
    private val mMeLinker: QNMicLinker?
        get() {
            return context.getMicLinker(user?.userId ?: "hjhb")
        }

    private fun compare(old: List<QNMicLinker>, new: List<QNMicLinker>): Boolean {
        if (old.size - new.size != 1) {
            return false
        }
        var oldKey = ""
        old.forEach {
            if (it.user.userId == roomInfo?.anchorInfo?.userId) {

            } else {
                oldKey += it.user.userId
            }
        }
        var newKey = ""

        new.forEach {
            if (it.user.userId == roomInfo?.anchorInfo?.userId) {
            } else {
                newKey += it.user.userId
            }
        }
        return newKey == oldKey
    }

    private val mMicListJob = Scheduler(5000) {
        if (roomInfo == null) {
            return@Scheduler
        }
        if (isLinked()) {
            return@Scheduler
        }
        backGround {
            doWork {
                val list = mLinkDateSource.getMicList(roomInfo?.liveId ?: "")

                if (compare(context.allLinker, list)) {
                    return@doWork
                }
                val toRemve = LinkedList<QNMicLinker>()

                context.allLinker.forEach { old ->
                    var isContainer = false
                    if (old.user.userId == roomInfo?.anchorInfo?.userId) {
                        isContainer = true
                    } else {
                        list.forEach { new ->
                            if (new.user.userId == old.user.userId) {
                                isContainer = true
                            }
                        }
                    }
                    if (!isContainer) {
                        toRemve.add(old)
                    }
                }

                context.allLinker.removeAll(toRemve)
                list.forEach {
                    context.addLinker(it)
                }
                context.mMicLinkerListeners.forEach {
                    it.onInitLinkers(context.allLinker)
                }
            }
        }
    }


    /**
     *  添加连麦监听
     */
    override fun addLinkMicListener(listener: QNAudienceMicLinker.LinkMicListener) {
        mLinkMicListeners.add(listener)
    }

    /**
     * 移除连麦监听
     */
    override fun removeLinkMicListener(listener: QNAudienceMicLinker.LinkMicListener) {
        mLinkMicListeners.remove(listener)
    }

    private val mAudienceExtQNClientEventListener = object : DefaultExtQNClientEventListener {
        override fun onConnectionStateChanged(
            p0: QNConnectionState,
            p1: QNConnectionDisconnectedInfo?
        ) {
            mLinkMicListeners.forEach {
                it.onConnectionStateChanged(p0)
            }
            if (p0 == QNConnectionState.DISCONNECTED) {
                if (mMeLinker != null) {
                    stopInner(
                        true, null
                    )
                }
            }
        }
    }

    override fun attachRoomClient(client: QNLiveRoomClient) {
        super.attachRoomClient(client)
        context.mRtcLiveRoom = RtcLiveRoom(AppCache.appContext)
        context.mRtcLiveRoom.addExtraQNRTCEngineEventListener(context.mExtQNClientEventListener)
        context.mRtcLiveRoom.addExtraQNRTCEngineEventListener(mAudienceExtQNClientEventListener)

    }

    override fun onRoomClose() {
        super.onRoomClose()
        context.mRtcLiveRoom.close()
    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        mMicListJob.start()
    }

    /**
     * 开始上麦
     *
     * @param cameraParams
     * @param microphoneParams
     * @param callBack         上麦成功失败回调
     */
    override fun startLink(
        extensions: HashMap<String, String>?, cameraParams: QNCameraParams?,
        microphoneParams: QNMicrophoneParams?, callBack: QNLiveCallBack<Void>?
    ) {
        mMicListJob.cancel()
        if (roomInfo == null) {
            callBack?.onError(-1, "roomInfo==null")
            mMicListJob.start(true)
            return
        }
        val linker = QNMicLinker()

        linker.extensions = extensions
        linker.isOpenCamera = cameraParams != null
        linker.isOpenMicrophone = microphoneParams != null
        linker.userRoomId = roomInfo?.liveId ?: ""

        val msg = RtmTextMsg<QNMicLinker>(
            liveroom_miclinker_join,
            linker
        ).toJsonString()

        backGround {
            doWork {

                val token = mLinkDateSource.upMic(linker)

                linker.user = user
                RtmManager.rtmClient.sendChannelMsg(
                    RtmTextMsg<QNMicLinker>(
                        liveroom_miclinker_join,
                        linker
                    ).toJsonString(),
                    roomInfo!!.chatId, false
                )

                cameraParams?.let {
                    context.mRtcLiveRoom.enableCamera(it)
                }

                microphoneParams?.let {
                    context.mRtcLiveRoom.enableMicrophone(it)
                }
                context.mRtcLiveRoom.joinRtc(token.rtc_token, JsonUtils.toJson(linker))

//                val users = ArrayList<QNMicLinker>()
//                context.mRtcLiveRoom.mClient.remoteUsers.forEach {
//                    if (it.userID != roomInfo?.anchorInfo?.userId) {
//                        val linck = JsonUtils.parseObject(it.userData, QNMicLinker::class.java)
//                    }
//                }

                context.mRtcLiveRoom.publishLocal()
                mLinkMicListeners.forEach {
                    it.lonLocalRoleChange(true)
                }
                context.mExtQNClientEventListener.onUserJoined(
                    user?.userId ?: "",
                    JsonUtils.toJson(linker)
                )

                mPlayer?.changeClientRole(ClientRoleType.ROLE_PUSH)
                callBack?.onSuccess(null)
            }
            catchError {
                mMicListJob.start(true)
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 我是不是麦上用户
     */
    override fun isLinked(): Boolean {
        return mMeLinker != null
    }

    /**
     * 结束连麦
     */
    override fun stopLink(callBack: QNLiveCallBack<Void>?) {
        stopInner(false, callBack)
    }

    fun stopInner(force: Boolean, callBack: QNLiveCallBack<Void>?, sendMsg: Boolean = true) {
        if (mMeLinker == null) {
            callBack?.onError(-1, "user is not on mic")
            return
        }
        backGround {
            doWork {
                Log.d("QNAudience", "下麦 ")
                mLinkDateSource.downMic(mMeLinker!!)
                val mode = UidMode().apply {
                    uid = user?.userId
                }
                if (force) {

                } else {

                }
                if (sendMsg) {
                    try {
                        RtmManager.rtmClient.sendChannelMsg(
                            RtmTextMsg<UidMode>(
                                liveroom_miclinker_left,
                                mode
                            ).toJsonString(),
                            roomInfo!!.chatId, false
                        )
                    } catch (e: RtmException) {
                        e.printStackTrace()
                    }
                }
                context.mRtcLiveRoom.leave()

                if (sendMsg) {
                    context.mExtQNClientEventListener.onUserLeft(
                        user?.userId ?: ""
                    )
                }
                Log.d("QNAudience", "下麦 1")
                context.removeLinker(user!!.userId)
                mLinkMicListeners.forEach {
                    it.lonLocalRoleChange(false)
                }
                mPlayer?.changeClientRole(ClientRoleType.ROLE_PULL)
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun switchCamera() {
        if (mMeLinker == null) {
            return
        }
        context.mRtcLiveRoom.switchCamera()
    }

    override fun muteLocalCamera(muted: Boolean, callBack: QNLiveCallBack<Void>?) {
        if (mMeLinker == null) {
            return
        }
        val mode = MuteMode().apply {
            uid = user?.userId
            mute = muted
        }

        backGround {
            doWork {
                mLinkDateSource.switch(
                    mMeLinker!!, false, !muted
                )
                RtmManager.rtmClient.sendChannelMsg(
                    RtmTextMsg<MuteMode>(
                        liveroom_miclinker_camera_mute,
                        mode
                    ).toJsonString(),
                    roomInfo!!.chatId, true
                )
                context.mRtcLiveRoom.muteLocalCamera(muted)
                mMeLinker?.isOpenCamera = !muted
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)

            }
        }
    }

    override fun muteLocalMicrophone(muted: Boolean, callBack: QNLiveCallBack<Void>?) {
        if (mMeLinker == null) {
            return
        }
        val mode = MuteMode().apply {
            uid = user?.userId
            mute = muted
        }
        backGround {
            doWork {
                mLinkDateSource.switch(
                    mMeLinker!!, true, !muted
                )
                RtmManager.rtmClient.sendChannelMsg(
                    RtmTextMsg<MuteMode>(
                        liveroom_miclinker_microphone_mute,
                        mode
                    ).toJsonString(),
                    roomInfo!!.chatId, true
                )
                context.mRtcLiveRoom.muteLocalMicrophone(muted)

                mMeLinker?.isOpenMicrophone = !muted
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun setVideoFrameListener(frameListener: QNVideoFrameListener) {
        context.mRtcLiveRoom.setVideoFrameListener(frameListener)
    }

    override fun setAudioFrameListener(frameListener: QNAudioFrameListener) {
        context.mRtcLiveRoom.setAudioFrameListener(frameListener)
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        mMicListJob.cancel()
        if (mMeLinker != null) {
            stopInner(true, null)
        }
    }

}