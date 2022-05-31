package com.qncube.linkmicservice;

import com.qiniu.droid.rtc.QNRenderView;
import com.qncube.liveroomcore.Extension;
import com.qncube.liveroomcore.QNLiveCallBack;
import com.qncube.liveroomcore.QNLiveService;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 连麦服务
 */
public interface QNLinkMicService extends QNLiveService {


    /**
     * 麦位监听
     */
    interface MicLinkerListener {

        /**
         * 观众初始化进入直播间 回调给观众当前有哪些人在连麦
         *
         * @param linkers
         */
        void onInitLinkers(@NotNull List<QNMicLinker> linkers);

        /**
         * 有人上麦
         *
         * @param micLinker
         */
        void onUserJoinLink(QNMicLinker micLinker);

        /**
         * 有人下麦
         *
         * @param micLinker
         */
        void onUserLeft(@NotNull  QNMicLinker micLinker);

        /**
         * 有人麦克风变化
         *
         * @param micLinker
         */
        void onUserMicrophoneStatusChange(@NotNull  QNMicLinker micLinker);

        /**
         * 有人摄像头状态变化
         *
         * @param micLinker
         */
        void onUserCameraStatusChange(@NotNull  QNMicLinker micLinker);

        /**
         * 有人被踢
         *
         * @param micLinker
         * @param msg 自定义扩展消息
         */
        void onUserBeKick(@NotNull  QNMicLinker micLinker, String msg);

        /**
         * 有人扩展字段变化
         *
         * @param micLinker
         * @param extension
         */
        void onUserExtension(@NotNull  QNMicLinker micLinker, Extension extension);
    }

    /**
     * 获取当前房间所有连麦用户
     * @return
     */
    List<QNMicLinker> getAllLinker();

    /**
     * 设置某人的连麦视频预览
     * 麦上用户调用  上麦后才会使用切换成rtc连麦 下麦后使用拉流预览
     * @param uid
     * @param preview
     */
    void setUserPreview(String uid, QNRenderView preview);


    /**
     * 踢人
     *
     * @param uid
     */
    void kickOutUser(String uid, String msg, QNLiveCallBack<Void> callBack);


    /**
     * 跟新扩展字段
     *
     * @param micLinker
     * @param extension
     */
    void updateExtension(@NotNull  QNMicLinker micLinker, Extension extension,QNLiveCallBack<Void> callBack);

    //添加监听
    void addMicLinkerListener(MicLinkerListener listener);

    void removeMicLinkerListener(MicLinkerListener listener);

    /**
     * 获得连麦邀请处理
     *
     * @return
     */
    QNLinkMicInvitationHandler getLinkMicInvitationHandler();

    /**
     * 观众向主播连麦
     *
     * @return
     */
    QNAudienceMicLinker getAudienceMicLinker();

    /**
     * 主播处理自己被连麦
     *
     * @return
     */
    QNAnchorHostMicLinker getAnchorHostMicLinker();


}
