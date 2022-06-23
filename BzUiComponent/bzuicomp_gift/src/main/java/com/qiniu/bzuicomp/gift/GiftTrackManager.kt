package com.qiniu.bzuicomp.gift

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.niucube.comproom.RoomManager
import com.niucube.rtm.RtmManager
import com.niucube.rtm.RtmMsgListener
import com.niucube.rtm.optAction
import com.niucube.rtm.optData
import com.qiniu.bzuicomp.gift.GiftMsg.action_gift
import com.qiniu.bzuicomp.pubchat.PubChatMsgManager
import com.qiniu.compui.trackview.SpanTrackManager
import com.qiniu.jsonutil.JsonUtils

class GiftTrackManager : SpanTrackManager<GiftMsg>(), LifecycleObserver {

    var extGiftMsgCall: ((giftMsg: GiftMsg) -> Unit)? = null

    private val mRtmChannelListener = object : RtmMsgListener {

        override fun onNewMsg(msg: String, fromId: String, toId: String): Boolean {
            if ((action_gift == msg.optAction()
                        )
            ) {
                JsonUtils.parseObject(msg.optData(), GiftMsg::class.java)?.let {
                    onNewTrackArrive(it)
                    extGiftMsgCall?.invoke(it)
                    PubChatMsgManager.onNewMsg(it)
                }
            }
            return false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        RtmManager.removeRtmChannelListener(mRtmChannelListener)
    }

    init {
        RtmManager.addRtmChannelListener(mRtmChannelListener)
    }
}