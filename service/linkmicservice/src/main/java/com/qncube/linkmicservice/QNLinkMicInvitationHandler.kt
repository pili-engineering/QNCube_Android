package com.qncube.linkmicservice

import com.qncube.liveroomcore.QNLiveCallBack
import java.util.HashMap

/**
 * 邀请处理器
 */
interface QNLinkMicInvitationHandler {
    //邀请监听
    interface InvitationListener {
        fun onReceivedApply(linkInvitation: LinkInvitation)
        fun onApplyCanceled(linkInvitation: LinkInvitation)
        fun onApplyTimeOut(linkInvitation: LinkInvitation)
        fun onAccept(linkInvitation: LinkInvitation)
        fun onReject(linkInvitation: LinkInvitation)
    }

    //注册邀请监听
    fun addInvitationLister(listener: InvitationListener)
    fun removeInvitationLister(listener: InvitationListener)

    /**
     * 邀请/申请
     */
    fun apply(
        expiration: Long,
        receiverRoomId: String,
        receiverUid: String,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<LinkInvitation>?
    )

    /**
     * 取消申请
     */
    fun cancelApply(invitationId: Int, callBack: QNLiveCallBack<Void>?)

    /**
     * 接受连麦
     */
    fun accept(
        invitationId: Int,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<Void>?
    )

    /**
     * 拒绝连麦
     */
    fun reject(
        invitationId: Int,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<Void>?
    )
}