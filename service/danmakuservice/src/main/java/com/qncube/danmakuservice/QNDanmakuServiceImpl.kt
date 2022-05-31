package com.qncube.danmakuservice

import com.niucube.rtm.*
import com.niucube.rtm.msg.RtmTextMsg
import com.qiniu.jsonutil.JsonUtils
import com.qncube.liveroomcore.BaseService
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.QNLiveRoomClient

class QNDanmakuServiceImpl : QNDanmakuService, BaseService() {
    private val mDanmakuServiceListeners = ArrayList<QNDanmakuService.QNDanmakuServiceListener>()
    private val rtmMsgListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromId: String, toId: String): Boolean {
            if (msg.optAction() == DanmakuModel.action_danmu) {
                val mode = JsonUtils.parseObject(msg, DanmakuModel::class.java) ?: return true
                mDanmakuServiceListeners.forEach {
                    it.onReceiveDanmaku(mode)
                }
                return true
            }
            return false
        }
    }

    override fun attachRoomClient(client: QNLiveRoomClient) {
        super.attachRoomClient(client)
        RtmManager.addRtmChannelListener(rtmMsgListener)
    }

    override fun onRoomClose() {
        super.onRoomClose()
        RtmManager.removeRtmChannelListener(rtmMsgListener)
    }

    override fun addDanmakuServiceListener(listener: QNDanmakuService.QNDanmakuServiceListener) {
        mDanmakuServiceListeners.add(listener)
    }

    override fun removeDanmakuServiceListener(listener: QNDanmakuService.QNDanmakuServiceListener) {
        mDanmakuServiceListeners.remove(listener)
    }

    /**
     * 发送弹幕消息
     */
    override fun sendDanmaku(
        msg: String,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<DanmakuModel>?
    ) {
        val mode = DanmakuModel().apply {
            sendUser = user
            content = msg
            senderRoomId = roomInfo?.liveId
            this.extensions = extensions
        }
        val rtmMsg = RtmTextMsg<DanmakuModel>(
            DanmakuModel.action_danmu,
            mode
        )
        RtmManager.rtmClient.sendChannelMsg(rtmMsg.toJsonString(), roomInfo?.chatId ?: "", true,
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack?.onSuccess(mode)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack?.onError(code, msg)
                }
            })
    }
}