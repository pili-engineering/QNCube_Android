package com.qncube.liveroomcore;

import com.qncube.liveroomcore.mode.QNLiveRoomInfo;
import com.qncube.liveroomcore.mode.QNLiveUser;

import org.jetbrains.annotations.NotNull;

/**
 * 房间生命周期
 */
public interface QNRoomLifeCycleListener {

    /**
     * 进入回调
     *
     * @param user
     */
    public void onRoomEnter(@NotNull  String liveId, @NotNull QNLiveUser user);

    /**
     * 加入回调
     *
     * @param roomInfo
     */
    public void onRoomJoined(@NotNull QNLiveRoomInfo roomInfo);

    /**
     * 离开回调
     */
    public void onRoomLeave();

    /**
     * 销毁回调
     */
    public void onRoomClose();
}
