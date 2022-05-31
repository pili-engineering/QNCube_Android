package com.qncube.linkmicservice;


import com.alibaba.fastjson.annotation.JSONField;
import com.qncube.liveroomcore.mode.QNLiveUser;

import java.util.HashMap;

/**
 * 连麦用户
 */
public class QNMicLinker {


    public QNLiveUser user;
    @JSONField(name = "live_id")
    public String userRoomId; //连麦用户所在房间ID
    //扩展字段
    @JSONField(name = "extends")
    public HashMap<String, String> extensions;

    /**
     * 是否开麦克风
     */
    @JSONField(name = "mic")
    public boolean isOpenMicrophone;
    /**
     * 是否开摄像头
     */
    @JSONField(name = "camera")
    public boolean isOpenCamera;
}

