package com.qiniu.bzuicomp.danmu

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.niucube.rtm.*
import com.niucube.comproom.RoomManager
import com.qiniu.bzuicomp.danmu.DanmuEntity.action_danmu

import com.niucube.rtm.msg.RtmTextMsg
import com.qiniu.compui.trackview.TrackManager
import com.qiniu.jsonutil.JsonUtils
import com.qiniusdk.userinfoprovide.UserInfoProvider

class DanmuTrackManager : TrackManager<DanmuEntity>(), LifecycleObserver {

    private val mRtmChannelListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, peerId: String): Boolean {

            if ((action_danmu == msg.optAction()
                        )
            ) {
                JsonUtils.parseObject(msg.optData(), DanmuEntity::class.java)?.let {
                    onNewTrackArrive(it)
                }
            }
            return false
        }
    }

    fun buidMsg(msgEdit: String) {
        val msg = RtmTextMsg<DanmuEntity>(
            action_danmu, (
                DanmuEntity().apply {
                    senderUid = UserInfoProvider.getLoginUserIdCall.invoke()
                    senderName = UserInfoProvider.getLoginUserNameCall.invoke()
                    content = msgEdit
                    senderRoomId = RoomManager.mCurrentRoom?.provideRoomId()
                    senderAvatar = UserInfoProvider.getLoginUserAvatarCall.invoke()
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
        RtmManager.addRtmChannelListener(mRtmChannelListener)
        RoomManager.addRoomLifecycleMonitor(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        RtmManager.removeRtmChannelListener(mRtmChannelListener)
        RoomManager.removeRoomLifecycleMonitor(this)
    }
}