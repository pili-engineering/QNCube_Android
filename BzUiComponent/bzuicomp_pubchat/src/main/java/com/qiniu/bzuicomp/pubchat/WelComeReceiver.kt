package com.qiniu.bzuicomp.pubchat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.niucube.rtm.*
import com.niucube.comproom.RoomManager
import com.niucube.rtm.msg.RtmTextMsg
import com.qiniu.jsonutil.JsonUtils
import com.qiniusdk.userinfoprovide.UserInfoProvider

class WelComeReceiver : LifecycleObserver {

    var userJoinCall={}
    private val channelListener = object : RtmMsgListener {
        /**
         * 收到群消息
         */
        override fun onNewMsg(msg: String, channelId: String) :Boolean{
            if ((PubChatWelCome.action_welcome == msg.optAction()
                        )
            ) {
                userJoinCall.invoke()
                JsonUtils.parseObject(msg.optData(), PubChatWelCome::class.java)?.let {
                    PubChatMsgManager.onNewMsg(it)
                }
                return true
            }
            if ((PubChatQuitRoom.action_quit_room == msg.optAction()
                        )
            ) {
                JsonUtils.parseObject(msg.optData(), PubChatQuitRoom::class.java)?.let {
                    PubChatMsgManager.onNewMsg(it)
                }
                return true
            }
            return false
        }
    }

    fun sendEnterMsg(msg:String="进入了房间"){
        val msg = RtmTextMsg<PubChatWelCome>(
            PubChatWelCome.action_welcome, (
                PubChatWelCome().apply {
                    senderId = UserInfoProvider.getLoginUserIdCall.invoke()
                    senderName = UserInfoProvider.getLoginUserNameCall.invoke()
                    msgContent = msg
                }))
        RtmManager.rtmClient.sendChannelMsg(msg.toJsonString(),
            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
            true,
            object : RtmCallBack {
                override fun onSuccess() {}
                override fun onFailure(code: Int, msg: String) {}
            })
    }

    fun sendQuitMsg(msg:String="退出了房间"){
        val msg = RtmTextMsg<PubChatQuitRoom>(
            PubChatQuitRoom.action_quit_room, (
                PubChatQuitRoom().apply {
                    senderId = UserInfoProvider.getLoginUserIdCall.invoke()
                    senderName = UserInfoProvider.getLoginUserNameCall.invoke()
                    msgContent = msg
                }))
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