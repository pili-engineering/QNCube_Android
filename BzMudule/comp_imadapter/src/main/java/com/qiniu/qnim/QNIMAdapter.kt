package com.qiniu.qnim

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.orhanobut.logger.Logger
import com.qiniu.ImLoginException
import com.niucube.rtm.RtmCallBack
import com.niucube.rtm.RtmAdapter
import com.qiniu.droid.imsdk.QNIMClient
import im.floo.floolib.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class QNIMAdapter : RtmAdapter {
    val LOG_TAG = "QNIm"
    var isInit = false
        private set

    var isLogin = false
        private set

    private var loginUid = ""
    private var loginImUid = ""
    private var mContext: Context? = null
    var onKickCall: () -> Unit = {}

    init {
        System.loadLibrary("floo")
    }

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
            val msgId = msg.clientTimestamp()
            val call: MsgCallTemp =
                mMsgCallMap.remove(msgId) ?: return

            GlobalScope.launch(Dispatchers.Main) {
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
                    Logger.t(LOG_TAG).d("onReceived  ${msgContent} ")

                    GlobalScope.launch(Dispatchers.Main) {
                        when (message.type()) {
                            BMXMessage.MessageType.Group -> {
                                channelMsgReceiver(msgContent, targetId)
                            }
                            BMXMessage.MessageType.Single -> {
                                c2cMessageReceiver(msgContent, targetId)
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
        }
    }
    private val mBMXUserServiceListener = object : BMXUserServiceListener() {
        override fun onConnectStatusChanged(status: BMXConnectStatus) {
            super.onConnectStatusChanged(status)
            Logger.t(LOG_TAG).d("ConnectionStatus ${status.name}")
        }

        override fun onOtherDeviceSingIn(deviceSN: Int) {
            super.onOtherDeviceSingIn(deviceSN)
            onKickCall.invoke()
        }
    }

    /**
     * 初始化
     */
    fun init(config: BMXSDKConfig, context: Context) {
        QNIMClient.init(config)
        mContext = context
        isInit = true
        QNIMClient.getUserManager().addUserListener(mBMXUserServiceListener)
    }

    fun loginOut() {
        QNIMClient.getUserManager().signOut {
        }
    }

    suspend fun loginSuspend(uid: String, loginImUid: String, name: String, pwd: String) =
        suspendCoroutine<Boolean> { continuation ->
            loginUid = uid
            this.loginImUid = loginImUid

            QNIMClient.getUserManager().signInByName(name, pwd) { p0 ->
                if (p0 == BMXErrorCode.NoError) {
                    isLogin = true
                    Logger.t(LOG_TAG).d("rongyun login onSuccess ")
                    continuation.resume(true)
                } else {
                    continuation.resumeWithException(
                        ImLoginException(
                            p0!!.swigValue(),
                            "im登录失败 ${p0?.name}"
                        )
                    )
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
            loginImUid.toLong(),
            peerId.toLong(),
            BMXMessage.MessageType.Single,
            peerId.toLong(),
            (msg)
        )

        val clientTime = System.currentTimeMillis()
        imMsg.setClientTimestamp(clientTime)

        mMsgCallMap.put(
            clientTime,
            MsgCallTemp(msg, peerId, isDispatchToLocal, callBack, false)
        )

        QNIMClient.sendMessage(imMsg)

    }

    override fun sendChannelMsg(
        msg: String,
        channelId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    ) {

        if (channelId.isEmpty()) {
            callBack?.onFailure(0, "")
            return
        }

        val imMsg = BMXMessage.createMessage(
            loginImUid.toLong(),
            channelId.toLong(),
            BMXMessage.MessageType.Group,
            channelId.toLong(),
            (msg)
        )

        val clientTime = System.currentTimeMillis()
        imMsg.setClientTimestamp(clientTime)

        mMsgCallMap.put(
            clientTime,
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
        Log.d(LOG_TAG, "joinChannel ${channelId}")
        QNIMClient.getChatRoomManager().join(channelId.toLong()) { p0 ->
            Log.d(LOG_TAG, "joinChannel callback ${p0.name}")
            if (p0 == BMXErrorCode.NoError || p0 == BMXErrorCode.GroupMemberExist) {
                try {
                    callBack?.onSuccess()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                callBack?.onFailure(p0.swigValue(), p0.name)
            }
        }
    }

    override fun leaveChannel(channelId: String, callBack: RtmCallBack?) {
        if (channelId.isEmpty()) {
            callBack?.onFailure(0, "channelId.isEmpty")
            return
        }
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
        return loginUid
    }

    /**
     * 注册监听
     * @param c2cMessageReceiver  c2c消息接收器
     * @param channelMsgReceiver 群消息接收器
     */
    override fun registerOriginImListener(
        c2cMessageReceiver: (msg: String, peerId: String) -> Unit,
        channelMsgReceiver: (msg: String, channel: String) -> Unit
    ) {
        this.c2cMessageReceiver = c2cMessageReceiver
        this.channelMsgReceiver = channelMsgReceiver
        QNIMClient.getChatManager().addChatListener(mChatListener)
    }
}