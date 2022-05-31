package com.qncube.linkmicservice

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
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.MuteMode
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.UidMode

class QNAudienceMicLinkerImpl(val context: MicLinkContext) : QNAudienceMicLinker, BaseService() {


    private val mLinkDateSource = LinkDateSource()

    private var mPlayer: IPullPlayer? = null
    private val mLinkMicListeners = ArrayList<QNAudienceMicLinker.LinkMicListener>()
    private val mMeLinker: QNMicLinker?
        get() {
            return context.getMicLinker(user?.userId ?: "hjhb")
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
        backGround {
            doWork {
                val result = mLinkDateSource.getMicList(roomInfo.liveId)
                result.forEach {
                    context.addLinker(it)
                }
                if (!result.isEmpty()) {
                    context.mMicLinkerListeners.forEach {
                        it.onInitLinkers(result)
                    }
                }
            }
        }
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
        if (roomInfo == null) {
            callBack?.onError(-1, "roomInfo==null")
            return
        }
        val linker = QNMicLinker()
        linker.user = user
        linker.extensions = extensions
        linker.isOpenCamera = cameraParams == null
        linker.isOpenMicrophone = microphoneParams == null
        linker.userRoomId = roomInfo?.liveId ?: ""

        val msg = RtmTextMsg<QNMicLinker>(
            liveroom_miclinker_join,
            linker
        ).toJsonString()

        backGround {
            doWork {


                mLinkDateSource.upMic(linker)
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
                context.mRtcLiveRoom.joinRtc("", msg)

                context.mExtQNClientEventListener.onUserJoined(
                    user?.userId ?: "",
                    JsonUtils.toJson(linker)
                )

                mLinkMicListeners.forEach {
                    it.lonLocalRoleChange(true)
                }

                mPlayer?.changeClientRole(ClientRoleType.ROLE_PUSH)
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 我是不是麦上用户
     */
    override fun isLinked(): Boolean {
        return mMeLinker == null
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
                mLinkMicListeners.forEach {
                    it.lonLocalRoleChange(false)
                }
                mPlayer?.changeClientRole(ClientRoleType.ROLE_PULL)

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
                    roomInfo!!.chatId, false
                )
                context.mRtcLiveRoom.muteLocalCamera(muted)
            }
            catchError {
                it.printStackTrace()
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
                    roomInfo!!.chatId, false
                )
                context.mRtcLiveRoom.muteLocalMicrophone(muted)
            }
            catchError {
                it.printStackTrace()
            }
        }

    }

    override fun setVideoFrameListener(frameListener: QNVideoFrameListener) {
        context.mRtcLiveRoom.setVideoFrameListener(frameListener)
    }

    override fun setAudioFrameListener(frameListener: QNAudioFrameListener) {
        context.mRtcLiveRoom.setAudioFrameListener(frameListener)
    }

    /**
     * 绑定原来的拉流预览
     * 连麦后 会禁用原来的拉流预览
     *
     * @param player
     */
    override fun attachPullPlayer(player: IPullPlayer) {
        mPlayer = player
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        if (mMeLinker != null) {
            stopInner(true, null)
        }
    }

}