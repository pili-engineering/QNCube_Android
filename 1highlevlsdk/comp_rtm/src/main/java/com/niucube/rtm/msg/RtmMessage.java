package com.niucube.rtm.msg;

public interface RtmMessage {

    /**
     * 获得消息类型
     */
    public MsgType getMsgType();

    /**
     * 获得信令码
     */
    public String getAction();
}
