package com.qiniu.bzuicomp.danmu;

import android.net.Uri;



public class DanmuEntity  {

    public static String action_danmu = "living_danmu";

    private String content;
    private String senderName;
    private String senderUid;
    private String senderRoomId;
    private String senderAvatar;


    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getSenderRoomId() {
        return senderRoomId;
    }

    public void setSenderRoomId(String senderRoomId) {
        this.senderRoomId = senderRoomId;
    }


}