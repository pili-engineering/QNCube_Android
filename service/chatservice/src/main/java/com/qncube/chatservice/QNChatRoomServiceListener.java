package com.qncube.chatservice;


import org.jetbrains.annotations.NotNull;

/**
 * 聊天室服务回调
 */
public interface QNChatRoomServiceListener {

    /**
     * 有人加入聊天室
     */
    void onUserJoin(@NotNull String memberId);

    /**
     * 有人离开聊天室
     */
    void onUserLevel(@NotNull String memberId);


    /**
     * 收到c2c消息
     */
    void onReceivedC2CMsg(@NotNull String msg,@NotNull  String fromId,@NotNull  String toId);

    /**
     * 收到群消息
     */
    void onReceivedGroupMsg(@NotNull String msg,@NotNull  String fromId, @NotNull String toId);


    /**
     * 有人被踢
     */
    void onUserBeKicked(@NotNull String memberId);

    /**
     * 有人被禁言
     */
    void onUserBeMuted(@NotNull boolean isMute, @NotNull  String memberId,@NotNull  long duration);

    void onAdminAdd(@NotNull String memberId);

    void onAdminRemoved(@NotNull String memberId,@NotNull  String reason);
}
