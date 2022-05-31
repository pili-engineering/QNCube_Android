package com.qncube.liveroomcore;

//插件 服务
public interface QNLiveService extends QNRoomLifeCycleListener {
    void attachRoomClient(QNLiveRoomClient client);
}