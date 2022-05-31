package com.qncube.linkmicservice

import com.niucube.rtm.*
import com.niucube.rtm.msg.RtmTextMsg
import com.qiniu.droid.rtc.QNRenderView
import com.qiniu.jsonutil.JsonUtils
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.*

class QNLinkMicServiceImpl : QNLinkMicService, BaseService() {

    companion object {
        val liveroom_miclinker_join = "liveroom_miclinker_join"
        val liveroom_miclinker_left = "liveroom_miclinker_left"
        val liveroom_miclinker_kick = "liveroom_miclinker_kick"
        val liveroom_miclinker_microphone_mute = "liveroom_miclinker_microphone_mute"
        val liveroom_miclinker_camera_mute = "liveroom_miclinker_camera_mute"
        val liveroom_miclinker_extension_change = "liveroom_miclinker_extension_change"
    }

    private val mLinkDateSource = LinkDateSource()
    private val mMicLinkContext = MicLinkContext()
    private val mAudienceMicLinker: QNAudienceMicLinkerImpl =
        QNAudienceMicLinkerImpl(mMicLinkContext)
    private val mAnchorHostMicLinker: QNAnchorHostMicLinkerImpl =
        QNAnchorHostMicLinkerImpl(mMicLinkContext)
    private val mLinkMicInvitationHandler = QNLinkMicInvitationHandlerImpl()

    private val mRtmMsgListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromId: String, toId: String): Boolean {
            when (msg.optAction()) {

                liveroom_miclinker_join -> {
                    if (mMicLinkContext.getMicLinker(user?.userId ?: "xasdaasda!#!@#") != null) {
                        return true
                    }
                    val micLinker =
                        JsonUtils.parseObject(msg.optData(), QNMicLinker::class.java) ?: return true
                    if (mMicLinkContext.addLinker(micLinker)) {
                        mMicLinkContext.mMicLinkerListeners.forEach {
                            it.onUserJoinLink(micLinker)
                        }
                    }
                }
                liveroom_miclinker_left -> {
                    if (mMicLinkContext.getMicLinker(user?.userId ?: "xasdaasda!#!@#") != null) {
                        return true
                    }
                    val micLinker =
                        JsonUtils.parseObject(msg.optData(), UidMode::class.java) ?: return true
                    mMicLinkContext.removeLinker(micLinker.uid)?.let { lincker ->
                        mMicLinkContext.mMicLinkerListeners.forEach {
                            it.onUserLeft(lincker)
                        }
                    }

                }
                liveroom_miclinker_kick
                -> {
                    val uidMsg =
                        JsonUtils.parseObject(msg.optData(), UidMsgMode::class.java) ?: return true

                    mMicLinkContext.removeLinker(uidMsg.uid)?.let { lincker ->
                        mMicLinkContext.mMicLinkerListeners.forEach {
                            it.onUserBeKick(lincker, uidMsg.msg)
                        }
                    }
                }
                liveroom_miclinker_microphone_mute
                -> {
                    val muteMode =
                        JsonUtils.parseObject(msg.optData(), MuteMode::class.java) ?: return true
                    mMicLinkContext.getMicLinker(muteMode.uid)?.let { linker ->
                        linker.isOpenMicrophone = !muteMode.mute
                        mMicLinkContext.mMicLinkerListeners.forEach {
                            it.onUserMicrophoneStatusChange(linker)
                        }
                    }
                }
                liveroom_miclinker_camera_mute
                -> {
                    val muteMode =
                        JsonUtils.parseObject(msg.optData(), MuteMode::class.java) ?: return true
                    mMicLinkContext.getMicLinker(muteMode.uid)?.let { linker ->
                        linker.isOpenCamera = !muteMode.mute
                        mMicLinkContext.mMicLinkerListeners.forEach {
                            it.onUserCameraStatusChange(linker)
                        }
                    }
                }
                liveroom_miclinker_extension_change
                -> {
                    val extMode =
                        JsonUtils.parseObject(msg.optData(), UidExtensionMode::class.java)
                            ?: return true
                    mMicLinkContext.getMicLinker(extMode.uid)?.let { linker ->
                        linker.extensions.put(extMode.extension.key, extMode.extension.value)
                        mMicLinkContext.mMicLinkerListeners.forEach {
                            it.onUserExtension(linker, extMode.extension)
                        }
                    }
                }
            }
            return false
        }
    }

    /**
     * 获取当前房间所有连麦用户
     * @return
     */
    override fun getAllLinker(): MutableList<QNMicLinker> {
        return mMicLinkContext.allLinker
    }

    /**
     * 设置某人的连麦视频预览
     * 麦上用户调用  上麦后才会使用切换成rtc连麦 下麦后使用拉流预览
     * @param uid
     * @param preview
     */
    override fun setUserPreview(uid: String, preview: QNRenderView) {
        mMicLinkContext.mRtcLiveRoom.setUserCameraWindowView(uid, preview)
    }

    /**
     * 踢人
     *
     * @param uid
     */
    override fun kickOutUser(uid: String, msg: String, callBack: QNLiveCallBack<Void>?) {
        val uidMsgMode = UidMsgMode()
        uidMsgMode.msg = msg
        uidMsgMode.uid = uid
        val rtmMsg = RtmTextMsg<UidMsgMode>(
            liveroom_miclinker_kick,
            uidMsgMode
        )
        RtmManager.rtmClient.sendChannelMsg(rtmMsg.toJsonString(), roomInfo?.liveId ?: "", true,
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack?.onSuccess(null)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack?.onError(code, msg)
                }
            })
    }

    /**
     * 跟新扩展字段
     *
     * @param micLinker
     * @param extension
     */
    override fun updateExtension(
        micLinker: QNMicLinker,
        extension: Extension,
        callBack: QNLiveCallBack<Void>?
    ) {
        backGround {
            doWork {
                mLinkDateSource.updateExt(micLinker, extension)
                val extMode = UidExtensionMode()
                extMode.uid = micLinker.user.userId
                extMode.extension = extension
                val rtmMsg =
                    RtmTextMsg<UidExtensionMode>(
                        liveroom_miclinker_extension_change,
                        extMode
                    )
                RtmManager.rtmClient.sendChannelMsg(
                    rtmMsg.toJsonString(),
                    roomInfo?.liveId ?: "",
                    true
                )
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }

    }

    override fun addMicLinkerListener(listener: QNLinkMicService.MicLinkerListener) {
        mMicLinkContext.mMicLinkerListeners.add(listener)
    }

    override fun removeMicLinkerListener(listener: QNLinkMicService.MicLinkerListener?) {
        mMicLinkContext.mMicLinkerListeners.remove(listener)
    }

    /**
     * 获得连麦邀请处理
     *
     * @return
     */
    override fun getLinkMicInvitationHandler(): QNLinkMicInvitationHandler {
        return mLinkMicInvitationHandler
    }

    /**
     * 观众向主播连麦
     *
     * @return
     */
    override fun getAudienceMicLinker(): QNAudienceMicLinker {
        return mAudienceMicLinker
    }

    /**
     * 主播处理自己被连麦
     *
     * @return
     */
    override fun getAnchorHostMicLinker(): QNAnchorHostMicLinker {
        return mAnchorHostMicLinker
    }

    override fun attachRoomClient(client: QNLiveRoomClient) {
        super.attachRoomClient(client)
        if (client.clientType == ClientType.CLIENT_PUSH) {
            mAnchorHostMicLinker.attachRoomClient(client)
        } else {
            mAudienceMicLinker.attachRoomClient(client)
        }
        mLinkMicInvitationHandler.attachRoomClient(client)
        RtmManager.addRtmChannelListener(mRtmMsgListener)
    }

    override fun onRoomEnter(roomId: String, user: QNLiveUser) {
        super.onRoomEnter(roomId, user)

        if (client!!.clientType == ClientType.CLIENT_PUSH) {
            mAnchorHostMicLinker.onRoomEnter(roomId, user)
        } else {
            mAudienceMicLinker.onRoomEnter(roomId, user)
        }
        mLinkMicInvitationHandler.onRoomEnter(roomId, user)
    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        //添加一个房主麦位
        mMicLinkContext.addLinker(QNMicLinker().apply {
            user = roomInfo.anchorInfo
            userRoomId = roomInfo.liveId
            isOpenMicrophone = true
            isOpenCamera = true
        })

        if (client!!.clientType == ClientType.CLIENT_PUSH) {
            mAnchorHostMicLinker.onRoomJoined(roomInfo)
        } else {
            mAudienceMicLinker.onRoomJoined(roomInfo)
        }

        mLinkMicInvitationHandler.onRoomJoined(roomInfo)
    }

    override fun onRoomLeave() {
        super.onRoomLeave()

        if (client!!.clientType == ClientType.CLIENT_PUSH) {
            mAnchorHostMicLinker.onRoomLeave()
        } else {
            mAudienceMicLinker.onRoomLeave()
        }

        mLinkMicInvitationHandler.onRoomLeave()
    }

    override fun onRoomClose() {
        super.onRoomClose()
        if (client!!.clientType == ClientType.CLIENT_PUSH) {
            mAnchorHostMicLinker.onRoomClose()
        } else {
            mAudienceMicLinker.onRoomClose()
        }

        mLinkMicInvitationHandler.onRoomClose()
        RtmManager.removeRtmChannelListener(mRtmMsgListener)
    }
}