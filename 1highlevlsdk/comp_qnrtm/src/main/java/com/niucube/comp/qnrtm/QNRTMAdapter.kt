package com.niucube.comp.qnrtm

import com.niucube.rtm.RtmAdapter
import com.niucube.rtm.RtmCallBack
import com.qiniu.droid.imsdk.QNIMClient
import im.floo.floolib.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class QNRTMAdapter(private val imUidGetter: () -> String) : RtmAdapter {

    class MsgCallTemp(
        val msg: String,
        val peerId: String,
        val isDispatchToLocal: Boolean,
        val callBack: RtmCallBack?,
        val isC2c: Boolean
    )

    private val mMsgCallMap: HashMap<Long, MsgCallTemp> = HashMap<Long, MsgCallTemp>()
    private var c2cMessageReceiver: (msg: String, peerId: String) -> Unit = { _, _ -> }
    private var channelMsgReceiver: (msg: String, peerId: String) -> Unit = { _, _ -> }
    private val mChatListener: BMXChatServiceListener = object : BMXChatServiceListener() {

        override fun onStatusChanged(msg: BMXMessage, error: BMXErrorCode) {
            super.onStatusChanged(msg, error)
            val call: MsgCallTemp =
                mMsgCallMap.remove(msg.msgId()) ?: return

            if (error == BMXErrorCode.NoError) {
                if (call.isDispatchToLocal) {
                    if (call.isC2c) c2cMessageReceiver(
                        call.msg,
                        call.peerId
                    ) else channelMsgReceiver(call.msg, call.peerId)
                }
                call.callBack?.onSuccess()
            } else {
                call.callBack?.onFailure(error.swigValue(), error.name)
            }
        }

        override fun onReceive(list: BMXMessageList) {
//收到消息
            if (list.isEmpty) {
                return
            }
            for (i in 0 until list.size().toInt()) {
                list[i]?.let { message ->
//目标ID
                    val targetId = message.toId().toString()
                    val msgContent = if (message.contentType() == BMXMessage.ContentType.Text
                        || message.contentType() == BMXMessage.ContentType.Command
                    ) {
                        message.content()
                    } else {
                        ""
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        when (message.type()) {
                            BMXMessage.MessageType.Group -> {
                                c2cMessageReceiver(msgContent, targetId)
                            }
                            BMXMessage.MessageType.Single -> {
                                channelMsgReceiver(msgContent, targetId)
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
        }
    }


    override fun sendC2cMsg(
        msg: String,
        peerId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    ) {
        //目前只处理文本消息
        val targetId = peerId
        val imMsg = BMXMessage.createMessage(
            imUidGetter.invoke().toLong(),
            peerId.toLong(),
            BMXMessage.MessageType.Single,
            peerId.toLong(),
            (msg)
        )
        mMsgCallMap.put(
            imMsg.msgId(),
            MsgCallTemp(msg, peerId, isDispatchToLocal, callBack, true)
        )
        QNIMClient.sendMessage(imMsg)

    }

    override fun sendChannelMsg(
        msg: String,
        channelId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    ) {
        val imMsg = BMXMessage.createMessage(
            imUidGetter.invoke().toLong(),
            channelId.toLong(),
            BMXMessage.MessageType.Group,
            channelId.toLong(),
            (msg)
        )
        mMsgCallMap.put(
            imMsg.msgId(),
            MsgCallTemp(msg, channelId, isDispatchToLocal, callBack, false)
        )
        QNIMClient.sendMessage(imMsg)
    }

    override fun createChannel(channelId: String, callBack: RtmCallBack?) {
        QNIMClient.getChatRoomManager().create(
            channelId
        ) { p0, p1 ->

            if (p0 == BMXErrorCode.NoError) {
                callBack?.onSuccess()
            } else {
                callBack?.onFailure(p0.swigValue(), p0.name)
            }
        }

    }

    override fun joinChannel(channelId: String, callBack: RtmCallBack?) {
        QNIMClient.getChatRoomManager().join(channelId.toLong()) { p0 ->
            if (p0 == BMXErrorCode.NoError) {
                callBack?.onSuccess()
            } else {
                callBack?.onFailure(p0.swigValue(), p0.name)
            }
        }
    }

    override fun leaveChannel(channelId: String, callBack: RtmCallBack?) {
        QNIMClient.getChatRoomManager().leave(channelId.toLong()) { p0 ->
            if (p0 == BMXErrorCode.NoError) {
                callBack?.onSuccess()
            } else {
                callBack?.onFailure(p0.swigValue(), p0.name)
            }
        }
    }

    override fun releaseChannel(channelId: String, callBack: RtmCallBack?) {
        //  自动销毁
    }

    override fun getLoginUserId(): String {
        return imUidGetter.invoke()
    }

    /**
     * 注册监听
     * @param c2cMessageReceiver  c2c消息接收器
     * @param channelMsgReceiver 群消息接收器
     */
    override fun registerOriginImListener(
        c2cReceiver: (msg: String, peerId: String) -> Unit,
        channelReceiver: (msg: String, peerId: String) -> Unit
    ) {
        c2cMessageReceiver = c2cReceiver
        channelMsgReceiver = channelReceiver
        QNIMClient.getChatManager().addChatListener(mChatListener)
    }


}
    
