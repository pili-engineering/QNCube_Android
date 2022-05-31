package com.qncube.liveroomcore;

import com.qncube.liveroomcore.mode.QNLiveRoomInfo;

import java.util.List;

//房间客户端
public interface QNLiveRoomClient {

    /**
     * 注册需要的服务
     *
     * @param serviceClass
     * @param <T>
     */
    <T extends QNLiveService> void registerService(Class<T> serviceClass);

    /**
     * 获取服务实例
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    <T extends QNLiveService> T getService(Class<T> serviceClass);

    /**
     * 添加房间生命周期状态监听
     *
     * @param lifeCycleListener
     */
    void addRoomLifeCycleListener(QNRoomLifeCycleListener lifeCycleListener);

    //移除房间生命周期状态监听
    void removeRoomLifeCycleListener(QNRoomLifeCycleListener lifeCycleListener);

    /**
     * 加入房间
     *
     * @param liveId
     * @param callBack
     */
    void joinRoom( String liveId, QNLiveCallBack<QNLiveRoomInfo> callBack);

    /**
     * 离开房间
     *
     * @param callBack
     */
    void leaveRoom(QNLiveCallBack<Void> callBack);

    /**
     * 关闭房间
     */
    void closeRoom();


    ClientType getClientType();
}
