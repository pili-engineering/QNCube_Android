package com.niucube.rtminvitation

interface InvitationCallBack {
    //收到邀请
     fun onReceiveInvitation(invitation: Invitation)
    //超时
     fun onInvitationTimeout(invitation: Invitation)
    //对方取消
     fun onReceiveCanceled(invitation: Invitation)
    //对方接受
     fun onInviteeAccepted(invitation: Invitation)
    //对方拒绝
     fun onInviteeRejected(invitation: Invitation)
     //挂掉
     fun onInviteeHangUp(invitation: Invitation){}
}