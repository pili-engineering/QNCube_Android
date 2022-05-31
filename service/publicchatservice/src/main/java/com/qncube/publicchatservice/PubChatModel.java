package com.qncube.publicchatservice;


import com.qncube.liveroomcore.mode.QNLiveUser;

import org.jetbrains.annotations.NotNull;

public class PubChatModel {
    public static String action_welcome = "liveroom-welcome";
    public static String action_bye = "liveroom-bye-bye";
    public static String action_like = "liveroom-like";
    public static String action_puchat = "liveroom-pubchat";
    public static String action_pubchat_custom = "liveroom-pubchat-custom";

    public String action;
    public QNLiveUser sendUser;
    /**
     * 消息内容
     */
    public String content;
    public String senderRoomId;


    /**
     * 消息类型
     * @return
     */
    @NotNull
    public String getMsgAction() {
        return action;
    }
}
