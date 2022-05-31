package com.niucube.rtminvitation

import android.text.TextUtils
import com.niucube.rtm.RtmCallBack
import com.niucube.rtm.RtmException
import com.niucube.rtm.RtmManager.rtmClient
import com.niucube.rtm.msg.RtmTextMsg
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun InvitationProcessor.suspendInvite(
    msg: String,
    peerId: String,
    channelId: String,
    timeoutThreshold: Long
) = suspendCoroutine<Invitation> { coti ->
    val invitationMsg = createInvitation(msg, peerId, channelId, timeoutThreshold)
    val rtmTextMsg = RtmTextMsg(InvitationProcessor.ACTION_SEND, invitationMsg)
    val call: RtmCallBack = object : RtmCallBack {
        override fun onSuccess() {
            addTimeOutRun(invitationMsg.invitation)
            coti.resume(invitationMsg.invitation)
        }
        override fun onFailure(code: Int, msg: String) {
            coti.resumeWithException(RtmException(code, msg))
        }
    }
    if (TextUtils.isEmpty(channelId)) {
        rtmClient.sendC2cMsg(rtmTextMsg.toJsonString(), peerId, false, call)
    } else {
        rtmClient.sendChannelMsg(rtmTextMsg.toJsonString(), channelId, false, call)
    }
}