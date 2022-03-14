package com.niucube.rtminvitation;
import java.io.Serializable;

public class Invitation implements Serializable {

    private int  flag ;// 邀请场次   内部维护
    private String msg ;//本次操作带的自定义数据。
    private long timeStamp; //时间戳
    private String initiatorUid;//
    private String receiver;//
    private String channelId ;//可为空 空代表c2c 。提供 set get


    public Invitation(){}

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getInitiatorUid() {
        return initiatorUid;
    }

    public void setInitiatorUid(String initiatorUid) {
        this.initiatorUid = initiatorUid;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }


}
