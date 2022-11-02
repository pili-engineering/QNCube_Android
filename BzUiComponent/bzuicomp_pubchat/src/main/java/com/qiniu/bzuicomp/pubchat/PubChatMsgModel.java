package com.qiniu.bzuicomp.pubchat;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;
import com.qiniu.bzuicomp.pubchat.IChatMsg;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class PubChatMsgModel implements IChatMsg, Serializable {

    public static String action_pubText="pub_chat_text";
    public String senderId;
    public String msgContent;
    public String sendAvatar;
    public String senderName;

    @JSONField(serialize = false)
    @NotNull
    @Override
    public String pubchat_getMsgHtml() {
        return   " <font color='#3ce1ff'>"+senderName+"</font>"+ " <font color='#ffb83c'>"+  " :"+msgContent+"</font>";
    }
    @JSONField(serialize = false)
    @NotNull
    @Override
    public String pubchat_getMsgAction() {
        return action_pubText;
    }

    @NonNull
    @Override
    public String pubchat_senderAvatar() {
        return sendAvatar;
    }

    @NonNull
    @Override
    public String pubchat_sendName() {
        return senderName;
    }

    @NonNull
    @Override
    public String pubchat_msgOrigin() {
        return senderName + " :" + msgContent;
    }

    @NonNull
    @Override
    public String pubchat_sendID() {
        return senderId;
    }
}
