package com.niucube.rtminvitation;

import android.os.Looper;
import android.text.TextUtils;

import com.niucube.rtm.RtmCallBack;
import com.niucube.rtm.RtmManager;
import com.niucube.rtm.msg.RtmTextMsg;
import com.qiniu.jsonutil.JsonUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


public class InvitationProcessor {

    public static String ACTION_SEND = "invite_send";
    public static String ACTION_CANCEL = "invite_cancel";
    public static String ACTION_ACCEPT = "invite_accept";
    public static String ACTION_REJECT = "invite_reject";
    public static String ACTION_HANGUP = "invite_hangUp";

    /**
     * 邀请 业务号 ex:"pk"
     */
    public String invitationName = "";
    public int flag = (int) (Math.random() * 1000);

    private android.os.Handler handler = new android.os.Handler(Looper.myLooper());

    private HashMap<Integer, Invitation> sentInvitations = new HashMap<Integer, Invitation>();
    private HashMap<Integer, Runnable> timeOutRuns = new HashMap<Integer, Runnable>();
    private InvitationCallBack mInvitationCallBack;

    /**
     * 构造方法
     *
     * @param invitationName 邀请业务名字 如 pk邀请 /上麦邀请 ...
     */
    public InvitationProcessor(String invitationName, InvitationCallBack invitationCallBack) {
        this.invitationName = invitationName;
        this.mInvitationCallBack = invitationCallBack;
    }

    /**
     * 发送邀请
     *
     * @param msg
     * @param peerId
     * @param channelId
     * @param callback
     * @return
     */
    public Invitation invite(String msg, String peerId, String channelId, long timeoutThreshold, RtmCallBack callback) {

        InvitationMsg invitation = createInvitation(msg, peerId, channelId, timeoutThreshold);
        RtmTextMsg<InvitationMsg> rtmTextMsg = new RtmTextMsg<InvitationMsg>(ACTION_SEND, invitation);

        RtmCallBack call = new RtmCallBack() {
            @Override
            public void onSuccess() {
                addTimeOutRun(invitation.getInvitation());
                callback.onSuccess();
            }

            @Override
            public void onFailure(int code, @NotNull String msg) {
                callback.onFailure(code, msg);
            }
        };

        if (TextUtils.isEmpty(channelId)) {
            RtmManager.INSTANCE.getRtmClient().sendC2cMsg(rtmTextMsg.toJsonString(), peerId, false, call);
        } else {
            RtmManager.INSTANCE.getRtmClient().sendChannelMsg(rtmTextMsg.toJsonString(), channelId, false, call);
        }
        return invitation.getInvitation();
    }

    public InvitationMsg createInvitation(String msg, String peerId, String channelId, long timeoutThreshold) {
        Invitation invitation = new Invitation();
        invitation.setMsg(msg);
        invitation.setChannelId(channelId);
        invitation.setInitiatorUid(RtmManager.INSTANCE.getRtmClient().getLoginUserId());
        invitation.setFlag(flag++);
        invitation.setReceiver(peerId);
        invitation.setTimeoutThreshold(timeoutThreshold);
        invitation.setTimeStamp(System.currentTimeMillis());
        InvitationMsg invitationMsg = new InvitationMsg();
        invitationMsg.setInvitation(invitation);
        invitationMsg.setInvitationName(invitationName);
        return invitationMsg;
    }

    protected void addTimeOutRun(Invitation invitation) {
        if (invitation.getTimeoutThreshold() >= 0) {
            Runnable timeOut = new Runnable() {
                @Override
                public void run() {
                    onInvitationTimeout(invitation);
                    reMoveTimeOutRun(invitation);
                }
            };
            sentInvitations.put(invitation.getFlag(), invitation);
            timeOutRuns.put(invitation.getFlag(), timeOut);
            handler.postDelayed(timeOut, invitation.getTimeoutThreshold());
        }
    }

    protected void reMoveTimeOutRun(Invitation invitation) {
        Runnable runnable = timeOutRuns.get(invitation.getFlag());
        sentInvitations.remove(invitation.getFlag());
        timeOutRuns.remove(invitation.getFlag());
        if (timeOutRuns != null) {
            handler.removeCallbacks(runnable);
        }
    }

    public final void cancel(Invitation invitation, RtmCallBack callback) {
        InvitationMsg invitationMsg = new InvitationMsg();
        invitationMsg.setInvitation(invitation);
        invitationMsg.setInvitationName(invitationName);
        RtmTextMsg<InvitationMsg> rtmTextMsg = new RtmTextMsg<InvitationMsg>(ACTION_CANCEL, (invitationMsg));

        reMoveTimeOutRun(invitation);

        if (TextUtils.isEmpty(invitation.getChannelId())) {
            RtmManager.INSTANCE.getRtmClient().sendC2cMsg(rtmTextMsg.toJsonString(), invitation.getInitiatorUid(), false, callback);
        } else {
            RtmManager.INSTANCE.getRtmClient().sendChannelMsg(rtmTextMsg.toJsonString(), invitation.getChannelId(), false, callback);
        }
    }

    public void accept(Invitation invitation, RtmCallBack callback) {
        InvitationMsg invitationMsg = new InvitationMsg();
        invitationMsg.setInvitation(invitation);
        invitationMsg.setInvitationName(invitationName);
        RtmTextMsg<InvitationMsg> rtmTextMsg = new RtmTextMsg<InvitationMsg>(ACTION_ACCEPT, (invitationMsg));
        if (TextUtils.isEmpty(invitation.getChannelId())) {
            RtmManager.INSTANCE.getRtmClient().sendC2cMsg(rtmTextMsg.toJsonString(), invitation.getInitiatorUid(), false, callback);
        } else {
            RtmManager.INSTANCE.getRtmClient().sendChannelMsg(rtmTextMsg.toJsonString(), invitation.getChannelId(), false, callback);
        }
        reMoveTimeOutRun(invitation);
    }

    public void reject(Invitation invitation, RtmCallBack callback) {
        InvitationMsg invitationMsg = new InvitationMsg();
        invitationMsg.setInvitation(invitation);
        invitationMsg.setInvitationName(invitationName);
        RtmTextMsg<InvitationMsg> rtmTextMsg = new RtmTextMsg<InvitationMsg>(ACTION_REJECT, (invitationMsg));
        if (TextUtils.isEmpty(invitation.getChannelId())) {
            RtmManager.INSTANCE.getRtmClient().sendC2cMsg(rtmTextMsg.toJsonString(), invitation.getInitiatorUid(), false, callback);
        } else {
            RtmManager.INSTANCE.getRtmClient().sendChannelMsg(rtmTextMsg.toJsonString(), invitation.getChannelId(), false, callback);
        }
        reMoveTimeOutRun(invitation);
    }

    public void hungUp(Invitation invitation, RtmCallBack callback) {
        InvitationMsg invitationMsg = new InvitationMsg();
        invitationMsg.setInvitation(invitation);
        invitationMsg.setInvitationName(invitationName);
        RtmTextMsg<InvitationMsg> rtmTextMsg = new RtmTextMsg<InvitationMsg>(ACTION_HANGUP, (invitationMsg));
        if (TextUtils.isEmpty(invitation.getChannelId())) {
            RtmManager.INSTANCE.getRtmClient().sendC2cMsg(rtmTextMsg.toJsonString(), invitation.getInitiatorUid(), false, callback);
        } else {
            RtmManager.INSTANCE.getRtmClient().sendChannelMsg(rtmTextMsg.toJsonString(), invitation.getChannelId(), false, callback);
        }
    }

    protected void onReceiveInvitation(Invitation invitation) {
        mInvitationCallBack.onReceiveInvitation(invitation);
    }

    /**
     * 邀请超时
     *
     * @param invitation
     */
    protected void onInvitationTimeout(Invitation invitation) {
        mInvitationCallBack.onInvitationTimeout(invitation);
    }

    /**
     * 对方取消
     *
     * @param invitation
     */
    protected void onReceiveCanceled(Invitation invitation) {
        mInvitationCallBack.onReceiveCanceled(invitation);
    }

    /**
     * 对方接受
     *
     * @param invitation
     */
    public void onInviteeAccepted(Invitation invitation) {
        mInvitationCallBack.onInviteeAccepted(invitation);
    }

    /**
     * 对方拒绝
     *
     * @param invitation
     */
    public void onInviteeRejected(Invitation invitation) {
        mInvitationCallBack.onInviteeRejected(invitation);
    }

    public void onInviteeHangUp(Invitation invitation) {
        mInvitationCallBack.onInviteeHangUp(invitation);
    }
}
