package com.niucube.absroom;
//禁言消息
public class ForbiddenMicSeatMsg {

    public String uid = "";
    public boolean isForbidden = false;
    public String msg = "";
    public ForbiddenMicSeatMsg(){}
    public ForbiddenMicSeatMsg(String uid, boolean isForbidden, String msg) {
        this.uid = uid;
        this.isForbidden = isForbidden;
        this.msg = msg;
    }
}
