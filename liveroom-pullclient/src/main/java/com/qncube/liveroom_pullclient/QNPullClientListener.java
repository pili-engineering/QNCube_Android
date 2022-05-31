package com.qncube.liveroom_pullclient;

//推流端房间状态监听
public interface QNPullClientListener {
    //直播房间状态变化
    public void onRoomStatusChange(int liveRoomStatus, String msg);
}