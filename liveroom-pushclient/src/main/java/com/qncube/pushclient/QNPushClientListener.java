package com.qncube.pushclient;

import com.qiniu.droid.rtc.QNConnectionState;
import com.qncube.liveroomcore.LiveStatus;

//推流客户端监听
public interface QNPushClientListener {

    /**
     * 推流连接状态
     *
     * @param state
     * @param msg
     */
    void onConnectionStateChanged(QNConnectionState state, String msg);

    /**
     * 房间状态
     *
     * @param liveRoomStatus
     * @param msg
     */
    void onRoomStatusChange(LiveStatus liveRoomStatus, String msg);

    /**
     * 摄像头状态回调
     *
     * @param isOpen
     */
    void onCameraStatusChange(boolean isOpen);

    /**
     * 本地麦克风状态
     *
     * @param isOpen
     */
    void onMicrophoneStatusChange(boolean isOpen);
}

