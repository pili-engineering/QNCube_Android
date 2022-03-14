package com.qiniu.bzuicomp.gift;

import androidx.annotation.NonNull;

import com.qiniu.bzuicomp.pubchat.IChatMsg;

public class GiftMsg implements IChatMsg {

    public static String action_gift = "living_gift";

    private String senderName;
    private String senderUid;
    private String senderRoomId;
    private String senderAvatar;
    private Gift sendGift;
    private int number = 0;

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

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public Gift getSendGift() {
        return sendGift;
    }

    public void setSendGift(Gift sendGift) {
        this.sendGift = sendGift;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @NonNull
    @Override
    public String getMsgHtml() {
        return " <font color='#3ce1ff'>" + senderName + "</font>" + " <font color='#ffb83c'>" + " :" + "送出" + number + "个" + "</font>" + "<img src='" + DataInterfaceNew.INSTANCE.getGiftIcon(Integer.parseInt(sendGift.getGiftId())) + "'>";
    }

    @NonNull
    @Override
    public String getMsgAction() {
        return action_gift;
    }
}
