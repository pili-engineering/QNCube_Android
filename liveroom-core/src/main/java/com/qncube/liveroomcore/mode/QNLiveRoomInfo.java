package com.qncube.liveroomcore.mode;

import com.alibaba.fastjson.annotation.JSONField;
import com.qncube.liveroomcore.mode.QNLiveUser;

import java.util.HashMap;
import java.util.Map;

public class QNLiveRoomInfo {

    @JSONField(name = "live_id")
    public String liveId;

    @JSONField(name = "title")
    public String title;

    @JSONField(name = "notice")
    public String notice;

    @JSONField(name = "cover_url")
    public String coverUrl;

    @JSONField(name = "extension")
    public Map<String, String> extension;

    @JSONField(name = "anchor_info")
    public QNLiveUser anchorInfo;

    @JSONField(name = "room_token")
    public String roomToken;

    @JSONField(name = "pk_id")
    public String pkId;

    @JSONField(name = "online_count")
    public long onlineCount;

    @JSONField(name = "start_time")
    public long startTime;

    @JSONField(name = "end_time")
    public long endTime;

    @JSONField(name = "chat_id")
    public String chatId;

    @JSONField(name = "push_url")
    public String pushUrl;

    @JSONField(name = "hls_url")
    public String hlsUrl;

    @JSONField(name = "rtmp_url")
    public String rtmpUrl;

    @JSONField(name = "flv_url")
    public String flvUrl;

    @JSONField(name = "pv")
    public Double pv;

    @JSONField(name = "uv")
    public Double uv;
    @JSONField(name = "total_count")
    public int totalCount;

    @JSONField(name = "total_mics")
    public int totalMics;

    @JSONField(name = "live_status")
    public int liveStatus;


}
