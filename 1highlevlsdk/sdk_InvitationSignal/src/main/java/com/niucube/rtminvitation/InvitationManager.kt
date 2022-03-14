package com.niucube.rtminvitation

import android.telephony.ims.ImsManager
import com.niucube.rtm.RtmManager
import com.niucube.rtm.RtmMsgListener
import com.niucube.rtm.optAction
import com.niucube.rtm.optData
import com.niucube.rtminvitation.InvitationProcessor.ACTION_HANGUP
import com.qiniu.jsonutil.JsonUtils

/**
 * 邀请系统
 */
object InvitationManager {

    private var mInvitationProcessor = ArrayList<InvitationProcessor>()

    private var mRtmMsgIntercept = object : RtmMsgListener {
        override fun onNewMsg(msg: String, peerId: String): Boolean {

            var isIntercept = false

            val action =msg.optAction()
            if (
                (action == InvitationProcessor.ACTION_SEND
                        || action == InvitationProcessor.ACTION_CANCEL
                        || action == InvitationProcessor.ACTION_ACCEPT
                        || action == InvitationProcessor.ACTION_REJECT)

            ) {
                isIntercept = true
                val invitationMsgModel =
                    JsonUtils.parseObject(msg.optData(), InvitationMsg::class.java)

                val invitation = invitationMsgModel?.invitation
                val invitationName = invitationMsgModel?.invitationName

                if(invitation?.receiver == RtmManager.rtmClient.getLoginUserId()
                    || invitation?.initiatorUid == RtmManager.rtmClient.getLoginUserId()){

                    mInvitationProcessor.forEach {
                        if (it.invitationName == invitationName) {
                            when (action) {
                                InvitationProcessor.ACTION_SEND -> {
                                    it.addTimeOutRun(invitation)
                                    it.onReceiveInvitation(invitation)
                                }
                                InvitationProcessor.ACTION_CANCEL -> {
                                    it.onReceiveCanceled(invitation)
                                }
                                InvitationProcessor.ACTION_ACCEPT -> {
                                    it.reMoveTimeOutRun(invitation)
                                    it.onInviteeAccepted(invitation)
                                }
                                InvitationProcessor.ACTION_REJECT -> {
                                    it.reMoveTimeOutRun(invitation)
                                    it.onInviteeRejected(invitation)
                                }
                                ACTION_HANGUP -> {
                                    it.onInviteeHangUp(invitation)
                                }
                            }
                        }
                    }
                }
            }
            return isIntercept
        }
    }

    init {
        RtmManager.addRtmC2cListener(mRtmMsgIntercept)
        RtmManager.addRtmChannelListener(mRtmMsgIntercept)
    }

    /**
     * 添加信令处理
     */
    fun addInvitationProcessor(invitationProcessor: InvitationProcessor) {
        mInvitationProcessor.add(invitationProcessor)
    }

    fun removeInvitationProcessor(invitationProcessor: InvitationProcessor) {
        mInvitationProcessor.remove(invitationProcessor)
    }
}