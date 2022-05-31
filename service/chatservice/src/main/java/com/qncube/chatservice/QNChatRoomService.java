package com.qncube.chatservice;


import com.qncube.liveroomcore.QNLiveCallBack;
import com.qncube.liveroomcore.QNLiveService;

/**
 * 聊天室服务
 */
public interface QNChatRoomService extends QNLiveService {

    public void addChatServiceListener(QNChatRoomServiceListener chatServiceListener);

    public void removeChatServiceListener(QNChatRoomServiceListener chatServiceListener);

    /**
     *  发c2c消息
     * @param msg
     * @param memberId
     * @param callBack
     */
    void sendCustomC2CMsg(String msg, String memberId, QNLiveCallBack<Void> callBack);

    /**
     * 发群消息
     * @param msg
     * @param callBack
     */
    void sendCustomGroupMsg(String msg, QNLiveCallBack<Void> callBack);


    /**
     * 踢人
     * @param msg
     * @param memberId
     * @param callBack
     */
    void kickUser(String msg, String memberId, QNLiveCallBack<Void> callBack);

    /**
     * 禁言
     * @param isMute
     * @param msg
     * @param memberId
     * @param duration
     * @param callBack
     */
    void muteUser(boolean isMute ,String msg, String memberId, long duration ,QNLiveCallBack<Void> callBack);

    /**
     * 添加管理员
     * @param memberId
     * @param callBack
     */
    void addAdmin( String memberId, QNLiveCallBack<Void> callBack);

    /**
     * 移除管理员
     * @param msg
     * @param memberId
     * @param callBack
     */
    void removeAdmin(String msg, String memberId, QNLiveCallBack<Void> callBack);

}
