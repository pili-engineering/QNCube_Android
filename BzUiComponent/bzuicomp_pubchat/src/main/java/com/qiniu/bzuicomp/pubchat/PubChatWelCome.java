package com.qiniu.bzuicomp.pubchat;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class PubChatWelCome implements IChatMsg, Serializable {

    public static String action_welcome="welcome";

    private String senderId;
    private String senderName;
    private String msgContent;
    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public String getSenderName() {
        return senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    public String getMsgContent() {
        return msgContent;
    }
    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    @NotNull
    @Override
    public String getMsgHtml() {
        return   " <font color='#3ce1ff'>"+senderName+"</font>"+ " <font color='#ffb83c'>"+  " :"+msgContent+"</font>";
    }

    @NotNull
    @Override
    public String getMsgAction() {
        return action_welcome;
    }
}
