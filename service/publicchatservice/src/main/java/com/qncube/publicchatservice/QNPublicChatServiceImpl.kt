package com.qncube.publicchatservice

import com.niucube.rtm.*
import com.niucube.rtm.msg.RtmTextMsg
import com.qiniu.jsonutil.JsonUtils
import com.qncube.liveroomcore.BaseService
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.QNLiveRoomClient
import java.util.*

class QNPublicChatServiceImpl : QNPublicChatService, BaseService() {

    private val mListeners = LinkedList<QNPublicChatService.QNPublicChatServiceLister>()

    private val mRtmMsgListener = object : RtmMsgListener {

        override fun onNewMsg(msg: String, fromId: String, toId: String): Boolean {
            if (
                msg.optAction() == PubChatModel.action_welcome ||
                msg.optAction() == PubChatModel.action_bye ||
                msg.optAction() == PubChatModel.action_like ||
                msg.optAction() == PubChatModel.action_puchat ||
                msg.optAction() == PubChatModel.action_pubchat_custom
            ) {
                val mode = JsonUtils.parseObject(msg.optData(), PubChatModel::class.java)
                mListeners.forEach {
                    it.onReceivePublicChat(mode)
                }
                return true
            }
            return false
        }
    }

    override fun attachRoomClient(client: QNLiveRoomClient) {
        super.attachRoomClient(client)
        RtmManager.addRtmChannelListener(mRtmMsgListener)
    }

    override fun onRoomClose() {
        super.onRoomClose()
        RtmManager.removeRtmChannelListener(mRtmMsgListener)
    }

    private fun sendModel(model: PubChatModel, callBack: QNLiveCallBack<PubChatModel>?) {
        val msg = RtmTextMsg(model.action, model).toJsonString()
        RtmManager.rtmClient.sendChannelMsg(
            msg,
            roomInfo?.chatId ?: "",
            true,
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack?.onSuccess(model)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack?.onError(code, msg)
                }
            })
    }

    /**
     * 发送 聊天室聊天
     * @param msg
     */
    override fun sendPublicChat(msg: String, callBack: QNLiveCallBack<PubChatModel>?) {
        sendModel(PubChatModel().apply {
            action = PubChatModel.action_puchat
            sendUser = user
            content = msg
            senderRoomId = roomInfo?.liveId
        }, callBack)
    }

    /**
     * 发送 欢迎进入消息
     *
     * @param msg
     */
    override fun sendWelCome(msg: String, callBack: QNLiveCallBack<PubChatModel>?) {
        sendModel(PubChatModel().apply {
            action = PubChatModel.action_welcome
            sendUser = user
            content = msg
            senderRoomId = roomInfo?.liveId
        }, callBack)
    }

    /**
     * 发送 拜拜
     *
     * @param msg
     */
    override fun sendByeBye(msg: String, callBack: QNLiveCallBack<PubChatModel>?) {
        sendModel(PubChatModel().apply {
            action = PubChatModel.action_bye
            sendUser = user
            content = msg
            senderRoomId = roomInfo?.liveId
        }, callBack)
    }

    /**
     * 点赞
     *
     * @param msg
     * @param callBack
     */
    override fun sendLike(msg: String, callBack: QNLiveCallBack<PubChatModel>?) {
        sendModel(PubChatModel().apply {
            action = PubChatModel.action_like
            sendUser = user
            content = msg
            senderRoomId = roomInfo?.liveId
        }, callBack)
    }

    /**
     * 自定义要显示在公屏上的消息
     *
     * @param action
     * @param msg
     * @param extensions
     * @param callBack
     */
    override fun sendCustomPubChat(
        act: String,
        msg: String,
        callBack: QNLiveCallBack<PubChatModel>?
    ) {
        val mode = PubChatModel().apply {
            action = act
            sendUser = user
            content = msg
            senderRoomId = roomInfo?.liveId
        }

        val rtmmsg = RtmTextMsg(
            PubChatModel.action_pubchat_custom,
            mode
        ).toJsonString()
        RtmManager.rtmClient.sendChannelMsg(
            rtmmsg,
            roomInfo?.chatId ?: "",
            false,
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack?.onSuccess(mode)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack?.onError(code, msg)
                }
            })
    }

    /**
     * 往本地公屏插入消息 不发送到远端
     */
    override fun pubLocalMsg(chatModel: PubChatModel) {
        mListeners.forEach {
            it.onReceivePublicChat(chatModel)
        }
    }

    override fun addPublicChatServiceLister(lister: QNPublicChatService.QNPublicChatServiceLister) {
        mListeners.add(lister)
    }

    override fun removePublicChatServiceLister(lister: QNPublicChatService.QNPublicChatServiceLister) {
        mListeners.remove(lister)
    }

}