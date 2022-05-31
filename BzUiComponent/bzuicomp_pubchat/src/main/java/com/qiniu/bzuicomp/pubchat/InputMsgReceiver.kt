package com.qiniu.bzuicomp.pubchat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.niucube.rtm.*
import com.niucube.comproom.RoomManager
import com.niucube.rtm.msg.RtmTextMsg
import com.qiniu.jsonutil.JsonUtils
import com.qiniusdk.userinfoprovide.UserInfoProvider

class InputMsgReceiver :LifecycleObserver{

    private val channelListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromId: String, toId: String):Boolean {
            if ((PubChatMsgModel.action_pubText == msg.optAction()
                        )
            ) {
                JsonUtils.parseObject(msg.optData(), PubChatMsgModel::class.java)?.let {
                    PubChatMsgManager.onNewMsg(it)
                }
                return true
            }
            return false
        }
    }

    fun buildMsg(msgEdit:String){
       val msg= RtmTextMsg<PubChatMsgModel>(
            PubChatMsgModel.action_pubText,(
                PubChatMsgModel().apply {
                    senderId = UserInfoProvider.getLoginUserIdCall.invoke()
                    senderName = UserInfoProvider.getLoginUserNameCall.invoke()
                    sendAvatar = UserInfoProvider.getLoginUserAvatarCall.invoke()
                    msgContent = msgEdit
                })
        )
        RtmManager.rtmClient.sendChannelMsg(msg.toJsonString(),
            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
            true,
            object : RtmCallBack {
                override fun onSuccess() {}
                override fun onFailure(code: Int, msg: String) {}
            })
    }
    init {
       RtmManager.addRtmChannelListener(channelListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        RtmManager.removeRtmChannelListener(channelListener)
    }
}