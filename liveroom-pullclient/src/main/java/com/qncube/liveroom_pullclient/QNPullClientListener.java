package com.qncube.liveroom_pullclient;

import com.qncube.liveroomcore.LiveStatus;

//推流端房间状态监听
public interface QNPullClientListener {
    //直播房间状态变化
    public void onRoomStatusChange(LiveStatus liveRoomStatus, String msg);
}