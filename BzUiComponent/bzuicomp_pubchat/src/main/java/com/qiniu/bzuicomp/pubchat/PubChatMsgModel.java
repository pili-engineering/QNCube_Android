package com.qiniu.bzuicomp.pubchat;

import com.alibaba.fastjson.annotation.JSONField;
import com.qiniu.bzuicomp.pubchat.IChatMsg;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class PubChatMsgModel implements IChatMsg, Serializable {

    public static String action_pubText="pub_chat_text";
    public String senderId;
    public String senderName;
    public String msgContent;
    public String sendAvatar;

    @JSONField(serialize = false)
    @NotNull
    @Override
    public String getMsgHtml() {
        return   " <font color='#3ce1ff'>"+senderName+"</font>"+ " <font color='#ffb83c'>"+  " :"+msgContent+"</font>";
    }
    @JSONField(serialize = false)
    @NotNull
    @Override
    public String getMsgAction() {
        return action_pubText;
    }
}
