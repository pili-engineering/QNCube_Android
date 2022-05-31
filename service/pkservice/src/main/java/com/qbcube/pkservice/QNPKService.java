package com.qbcube.pkservice;

import com.nucube.rtclive.MixStreamParams;
import com.nucube.rtclive.QNMergeOption;
import com.qiniu.droid.rtc.QNRenderView;
import com.qncube.liveroomcore.Extension;
import com.qncube.liveroomcore.QNLiveCallBack;
import com.qncube.liveroomcore.QNLiveService;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

/**
 * pk服务
 */
public interface QNPKService extends QNLiveService {

    /**
     * pk回调
     */
    public static interface PKServiceListener {

        /**
         * 用户进入房间 初始化返回当前房间的正在的pk
         *
         * @param pkSession
         */
        void onInitPKer(@NotNull QNPKSession pkSession);

        //开始
        void onStart(@NotNull QNPKSession pkSession);

        //结束
        void onStop(@NotNull QNPKSession pkSession, int code, @NotNull String msg);

        //pk 收对方流超时
        void onWaitPeerTimeOut(@NotNull QNPKSession pkSession);

        /**
         * 扩展自定义字段跟新
         *
         * @param extension
         */
        void onPKExtensionUpdate(@NotNull QNPKSession pkSession, @NotNull Extension extension);
    }

    /**
     * pk混流适配器
     */
    public static interface PKMixStreamAdapter {
        /**
         * 当pk开始 如何混流
         *
         * @param pkSession
         * @return 返回混流参数
         */
        List<QNMergeOption> onPKLinkerJoin(@NotNull QNPKSession pkSession);

        /**
         * pk开始时候混流画布变成多大
         *
         * @param pkSession
         * @return
         */
        MixStreamParams onPKMixStreamStart(@NotNull QNPKSession pkSession);

        /**
         * 当pk结束后如果还有其他普通连麦者 如何混流
         * 如果pk结束后没有其他连麦者 则不会回调
         *
         * @return
         */
        List<QNMergeOption> onPKLinkerLeft();
    }


    /**
     * 设置混流适配器
     *
     * @param adapter
     */
    void setPKMixStreamAdapter(PKMixStreamAdapter adapter);

    //添加监听
    void addPKServiceListener(PKServiceListener pkServiceListener);

    void removePKServiceListener(PKServiceListener pkServiceListener);

    /**
     * 跟新扩展自定义字段
     *
     * @param extension
     */
    void upDataPKExtension(Extension extension, QNLiveCallBack<Void> callBack);


    /**
     * 开始pk
     *
     * @param timeoutTimestamp 等待对方流超时时间时间戳 毫秒
     * @param receiverRoomId
     * @param receiverUid
     * @param extensions
     * @param callBack
     */
    void start(long timeoutTimestamp, String receiverRoomId, String receiverUid, HashMap<String, String> extensions, QNLiveCallBack<QNPKSession> callBack);

    //结束
    void stop(QNLiveCallBack<Void> callBack);

    /**
     * 设置某人的连麦预览
     *
     * @param uid  麦上用户ID
     * @param view
     */
    void setPeerAnchorPreView(String uid, QNRenderView view);

    /**
     * 获得pk邀请处理
     *
     * @return
     */
    QNPKInvitationHandler getPKInvitationHandler();

}
