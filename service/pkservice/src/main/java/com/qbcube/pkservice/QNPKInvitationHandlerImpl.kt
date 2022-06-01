package com.qbcube.pkservice

import com.niucube.rtm.RtmCallBack
import com.niucube.rtminvitation.*
import com.qiniu.jsonutil.JsonUtils
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.datasource.UserDataSource
import java.util.*
import kotlin.collections.HashMap

class QNPKInvitationHandlerImpl : QNPKInvitationHandler, BaseService() {

    private val mListeners = LinkedList<QNPKInvitationHandler.PKInvitationListener>()
    private val invitationMap = HashMap<Int, Invitation>()

    private val mInvitationProcessor =
        InvitationProcessor("liveroom-pk-invitation",
            object : InvitationCallBack {
                override fun onReceiveInvitation(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, PKInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    invitationMap[invitation.flag] = invitation
                    mListeners.forEach { it.onReceivedApply(linkInvitation) }
                }

                override fun onInvitationTimeout(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, PKInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    mListeners.forEach { it.onApplyTimeOut(linkInvitation) }
                    invitationMap.remove(invitation.flag)
                }

                override fun onReceiveCanceled(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, PKInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    mListeners.forEach { it.onApplyCanceled(linkInvitation) }
                    invitationMap.remove(invitation.flag)
                }

                override fun onInviteeAccepted(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, PKInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    mListeners.forEach { it.onAccept(linkInvitation) }
                    invitationMap.remove(invitation.flag)
                }

                override fun onInviteeRejected(invitation: Invitation) {
                    val linkInvitation =
                        JsonUtils.parseObject(invitation.msg, PKInvitation::class.java) ?: return
                    linkInvitation.invitationId = invitation.flag
                    mListeners.forEach { it.onReject(linkInvitation) }
                    invitationMap.remove(invitation.flag)
                }
            }
        )

    override fun addPKInvitationListener(listener: QNPKInvitationHandler.PKInvitationListener) {
        mListeners.add(listener)
    }

    override fun removePKInvitationListener(listener: QNPKInvitationHandler.PKInvitationListener) {
        mListeners.remove(listener)
    }

    /**
     * 申请pk
     */
    override fun applyJoin(
        expiration: Long,
        receiverRoomId: String,
        receiverUid: String,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<PKInvitation>?
    ) {
        if (roomInfo == null) {
            callBack?.onError(-1, "roomInfo==null")
            return
        }

        backGround {
            doWork {
                val receiver =
                    UserDataSource().searchUserByUserId(receiverUid)
                val pkInvitation = PKInvitation()
                pkInvitation.extensions = extensions
                pkInvitation.initiator = user
                pkInvitation.initiatorRoomId = roomInfo?.liveId
                pkInvitation.receiver = receiver
                pkInvitation.receiverRoomId = receiverRoomId

                val iv = mInvitationProcessor.suspendInvite(
                    JsonUtils.toJson(pkInvitation),
                    receiver.imUid, "", expiration
                )
                pkInvitation.invitationId = iv.flag
                invitationMap[iv.flag] = iv


                callBack?.onSuccess(pkInvitation)
            }

            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 取消申请
     */
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
            JsonUtils.parseObject(invitation.msg, PKInvitation::class.java) ?: return
        extensions?.entries?.forEach {
            linkInvitation.extensions[it.key] = it.value
        }
        invitation.msg = JsonUtils.toJson(linkInvitation)
        mInvitationProcessor.accept(invitation, object : RtmCallBack {
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
            JsonUtils.parseObject(invitation.msg, PKInvitation::class.java) ?: return
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


    override fun onRoomClose() {
        super.onRoomClose()
        InvitationManager.removeInvitationProcessor(mInvitationProcessor)
    }
    override fun attachRoomClient(client: QNLiveRoomClient) {
        super.attachRoomClient(client)
        InvitationManager.addInvitationProcessor(mInvitationProcessor)
    }
}