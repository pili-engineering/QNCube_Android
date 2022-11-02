package com.qiniu.bzuicomp.pubchat;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class PubChatQuitRoom implements IChatMsg, Serializable {

    public static String action_quit_room = "quit_room";
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
    @NonNull
    @Override
    public String pubchat_senderAvatar() {
        return "";
    }

    @NonNull
    @Override
    public String pubchat_sendName() {
        return senderName;
    }
    @NotNull
    @Override
    public String pubchat_getMsgHtml() {
        return " <font color='#3ce1ff'>" + senderName + "</font>" + " <font color='#ffb83c'>" + " :" + msgContent + "</font>";
    }

    @NotNull
    @Override
    public String pubchat_getMsgAction() {
        return action_quit_room;
    }

    @NonNull
    @Override
    public String pubchat_msgOrigin() {
        return senderName + " :" + msgContent;
    }

    @NonNull
    public String pubchat_sendID() {
        return senderId;
    }
}
