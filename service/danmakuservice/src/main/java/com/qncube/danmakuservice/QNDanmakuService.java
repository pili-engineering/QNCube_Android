package com.qncube.danmakuservice;


import com.qncube.liveroomcore.QNLiveCallBack;
import com.qncube.liveroomcore.QNLiveService;

import java.util.HashMap;

/**
 * 弹幕服务
 */
public interface QNDanmakuService extends QNLiveService {

    public interface QNDanmakuServiceListener {

        /**
         * 收到弹幕消息
         */
        void onReceiveDanmaku(DanmakuModel model);
    }

    public void addDanmakuServiceListener(QNDanmakuServiceListener listener);

    public void removeDanmakuServiceListener(QNDanmakuServiceListener listener);

    /**
     * 发送弹幕消息
     */
    public void sendDanmaku(String msg, HashMap<String,String> extensions, QNLiveCallBack<DanmakuModel> callBack);
}

