package com.qncube.publicchatservice;

import com.qncube.liveroomcore.QNLiveCallBack;
import com.qncube.liveroomcore.QNLiveService;

import java.util.HashMap;

public interface QNPublicChatService extends QNLiveService {


    //消息监听
    public static interface QNPublicChatServiceLister {

        /**
         * 收到公聊消息
         *
         * @param model
         */
        void onReceivePublicChat(PubChatModel model);
    }


    /**
     * 发送 聊天室聊天
     *
     * @param msg
     */
    public void sendPublicChat(String msg, QNLiveCallBack<PubChatModel> callBack);

    /**
     * 发送 欢迎进入消息
     *
     * @param msg
     */
    public void sendWelCome(String msg, QNLiveCallBack<PubChatModel> callBack);

    /**
     * 发送 拜拜
     *
     * @param msg
     */
    public void sendByeBye(String msg, QNLiveCallBack<PubChatModel> callBack);

    /**
     * 点赞
     *
     * @param msg
     * @param callBack
     */
    public void sendLike(String msg, QNLiveCallBack<PubChatModel> callBack);

    /**
     * 自定义要显示在公屏上的消息
     *
     * @param action
     * @param msg
     * @param callBack
     */
    public void sendCustomPubChat(String action, String msg,  QNLiveCallBack<PubChatModel> callBack);


    /**
     * 往本地公屏插入消息 不发送到远端
     */
    public void pubLocalMsg(PubChatModel chatModel);

    public void addPublicChatServiceLister(QNPublicChatServiceLister lister);

    public void removePublicChatServiceLister(QNPublicChatServiceLister lister);
}
