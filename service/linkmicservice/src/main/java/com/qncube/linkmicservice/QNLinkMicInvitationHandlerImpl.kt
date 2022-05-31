package com.qncube.linkmicservice

import com.niucube.rtm.RtmCallBack
import com.niucube.rtminvitation.Invitation
import com.niucube.rtminvitation.InvitationCallBack
import com.niucube.rtminvitation.InvitationProcessor
import com.niucube.rtminvitation.suspendInvite
import com.qiniu.jsonutil.JsonUtils
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.datasource.UserDataSource
import java.util.*
import kotlin.collections.HashMap

class QNLinkMicInvitationHandlerImpl : QNLinkMicInvitationHandler, BaseService() {

    private val listeners = LinkedList<QNLinkMicInvitationHandler.InvitationListener>()
    private val invitationMap = HashMap<Int, Invitation>()

    private val mInvitationProcessor =
        InvitationProcessor("liveroom-linkmic-invitation",
            object : InvitationCallBack {

                override fun onReceiveInvitation(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, LinkInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    invitationMap[invitation.flag] = invitation
                    listeners.forEach { it.onAccept(linkInvitation) }
                }

                override fun onInvitationTimeout(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, LinkInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    listeners.forEach { it.onApplyTimeOut(linkInvitation) }
                    invitationMap.remove(invitation.flag)
                }

                override fun onReceiveCanceled(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, LinkInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    listeners.forEach { it.onApplyCanceled(linkInvitation) }
                    invitationMap.remove(invitation.flag)
                }

                override fun onInviteeAccepted(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, LinkInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    listeners.forEach { it.onAccept(linkInvitation) }
                    invitationMap.remove(invitation.flag)
                }

                override fun onInviteeRejected(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, LinkInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    listeners.forEach { it.onReject(linkInvitation) }
                    invitationMap.remove(invitation.flag)
                }
            })

    override fun addInvitationLister(listener: QNLinkMicInvitationHandler.InvitationListener) {
        listeners.add(listener)
    }

    override fun removeInvitationLister(listener: QNLinkMicInvitationHandler.InvitationListener) {
        listeners.remove(listener)
    }

    /**
     * 邀请/申请
     */
    override fun apply(
        expiration: Long,
        receiverRoomId: String,
        receiverUid: String,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<LinkInvitation>?
    ) {
        if (roomInfo == null) {
            callBack?.onError(0, "roomInfo == null)")
            return
        }
        backGround {
            doWork {
                val receiver =
                    UserDataSource().searchUserByIMUid(receiverUid)

                val linkInvitation = LinkInvitation()
                linkInvitation.linkType = 1
                linkInvitation.initiator = user
                linkInvitation.initiatorRoomId = roomInfo?.liveId
                linkInvitation.extensions = extensions
                linkInvitation.receiver = receiver
                linkInvitation.receiverRoomId = receiverRoomId
                val channel = if (receiverRoomId != roomInfo!!.liveId) {
                    roomInfo!!.chatId
                } else {
                    ""
                }
                val iv = mInvitationProcessor.suspendInvite(
                    JsonUtils.toJson(linkInvitation),
                    receiver.imUid, channel, expiration
                )
                linkInvitation.invitationId = iv.flag
                invitationMap[iv.flag] = iv
                callBack?.onSuccess(linkInvitation)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 取消申请
     */
    override fun cancelApply(invitationId: Int, callBack: QNLiveCallBack<Void>?) {
        mInvitationProcessor.cancel(invitationMap[invitationId], object : RtmCallBack {
            override fun onSuccess() {
                invitationMap.remove(invitationId)
                callBack?.onSuccess(null)
            }

            override fun onFailure(code: Int, msg: String) {
                callBack?.onError(code, msg)
            }
        })
    }

    /**
     * 接受连麦
     */
    override fun accept(
        invitationId: Int,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<Void>?
    ) {
        val invitation = invitationMap[invitationId]
        if (invitation == null) {
            callBack?.onError(-1, "invitation==null")
            return
        }
        val linkInvitation =
            JsonUtils.parseObject(invitation.msg, LinkInvitation::class.java) ?: return
        extensions?.entries?.forEach {
            linkInvitation.extensions[it.key] = it.value
        }
        invitation.msg = JsonUtils.toJson(linkInvitation)
        mInvitationProcessor.cancel(invitation, object : RtmCallBack {
            override fun onSuccess() {
                invitationMap.remove(invitationId)
                callBack?.onSuccess(null)
            }

            override fun onFailure(code: Int, msg: String) {
                callBack?.onError(code, msg)
            }
        })

    }

    /**
     * 拒绝连麦
     */
    override fun reject(
        invitationId: Int,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<Void>?
    ) {
        val invitation = invitationMap[invitationId]
        if (invitation == null) {
            callBack?.onError(-1, "invitation==null")
            return
        }
        val linkInvitation =
            JsonUtils.parseObject(invitation.msg, LinkInvitation::class.java) ?: return
        extensions?.entries?.forEach {
            linkInvitation.extensions[it.key] = it.value
        }
        invitation.msg = JsonUtils.toJson(linkInvitation)
        mInvitationProcessor.reject(invitation, object : RtmCallBack {
            override fun onSuccess() {
                invitationMap.remove(invitationId)
                callBack?.onSuccess(null)
            }

            override fun onFailure(code: Int, msg: String) {
                callBack?.onError(code, msg)
            }
        })
    }

}