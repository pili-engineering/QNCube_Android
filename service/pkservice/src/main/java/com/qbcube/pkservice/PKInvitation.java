package com.qbcube.pkservice;

import com.alibaba.fastjson.annotation.JSONField;
import com.qncube.liveroomcore.mode.QNLiveUser;

import java.util.HashMap;

// pk邀请
public class PKInvitation {

    public QNLiveUser initiator;
    public QNLiveUser receiver;
    public String initiatorRoomId;
    public String receiverRoomId;
    public HashMap<String,String> extensions;


    //邀请ID
    @JSONField(serialize = false)
    public int invitationId;
}
