package com.qncube.linkmicservice;

import com.alibaba.fastjson.annotation.JSONField;
import com.qncube.liveroomcore.mode.QNLiveUser;

import java.util.HashMap;

//连麦邀请
public class LinkInvitation {

    public QNLiveUser initiator;
    public QNLiveUser receiver;
    public String initiatorRoomId;
    public String receiverRoomId;
    public HashMap<String, String> extensions;
    //连麦类型 用户向主播连麦  / 主播跨房连麦
    public int linkType;


    @JSONField(serialize = false)
    public int invitationId;

}