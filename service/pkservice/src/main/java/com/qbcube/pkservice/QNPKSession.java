package com.qbcube.pkservice;

import com.qncube.liveroomcore.mode.QNLiveUser;

import java.util.HashMap;

public class QNPKSession {

    //PK场次ID
    public String sessionId;
    //发起方
    public QNLiveUser initiator;
    //接受方
    public QNLiveUser receiver;
    //发起方所在房间
    public String initiatorRoomId;
    //接受方所在房间
    public String receiverRoomId;
    //扩展字段
    public HashMap<String, String> extensions;
    //pk 状态 0邀请过程  1pk中 2结束 其他自定义状态比如惩罚时间
    public int status;
    //pk开始时间戳
    public long startTimeStamp;

}