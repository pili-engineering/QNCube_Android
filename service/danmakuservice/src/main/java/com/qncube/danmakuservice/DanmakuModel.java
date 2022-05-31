package com.qncube.danmakuservice;


import com.qncube.liveroomcore.mode.QNLiveUser;

import java.util.HashMap;

/**
 * 弹幕实体
 */
public class DanmakuModel {
    public static String action_danmu = "living_danmu";

    public QNLiveUser sendUser;
    /**
     * 消息内容
     */
    public String content;
    public String senderRoomId;
    /**
     * 扩展字段
     */
    public HashMap<String, String> extensions;
}


