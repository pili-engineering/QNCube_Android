package com.qncube.pushclient;

import com.nucube.rtclive.QNCameraParams;
import com.nucube.rtclive.QNMicrophoneParams;
import com.qiniu.droid.rtc.QNAudioFrameListener;
import com.qiniu.droid.rtc.QNRenderView;
import com.qiniu.droid.rtc.QNVideoFrameListener;
import com.qncube.liveroomcore.QNLiveRoomClient;

//推流客户端
public interface QNLivePushClient extends QNLiveRoomClient {

    //创建实例
    static QNLivePushClient createLivePushClient() {
        return new QNLivePushClientImpl();
    }

    //启动视频采集
    void enableCamera(QNCameraParams cameraParams);

    //启动音频采集
    void enableMicrophone(QNMicrophoneParams microphoneParams);

    //切换摄像头
    void switchCamera();

    //设置连接状态回调
    void setPushClientListener(QNPushClientListener pushClientListener);

    //设置本地预览
    void setLocalPreView(QNRenderView view);

    QNRenderView getLocalPreView();

    //禁/不禁 本地摄像头推流
    void muteLocalCamera(boolean muted);

    //禁/不禁 本地摄像头推流
    void muteLocalMicrophone(boolean muted);

    //设置视频帧回调
    void setVideoFrameListener(QNVideoFrameListener frameListener);

    //设置音频帧回调
    void setAudioFrameListener(QNAudioFrameListener frameListener);
}