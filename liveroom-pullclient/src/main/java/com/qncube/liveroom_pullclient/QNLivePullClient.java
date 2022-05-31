package com.qncube.liveroom_pullclient;

import com.qncube.liveroomcore.IPullPlayer;
import com.qncube.liveroomcore.QNLiveRoomClient;

/**
 *拉流客户端
 */
public interface QNLivePullClient extends QNLiveRoomClient {

    //创建实例
    static QNLivePullClient createLivePullClient(){
        return new QNLivePullClientImpl();
    }

    //拉流客户端监听
    void setPullClientListener(QNPullClientListener listener);

    //绑定播放器
    void setPullPreview(IPullPlayer player);

    IPullPlayer getPullPreview();

}


