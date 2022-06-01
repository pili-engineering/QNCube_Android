package com.qncube.liveroomcore;

import com.qncube.liveroomcore.mode.QNLiveRoomInfo;

public interface IPullPlayer {
    /**
     * 开始播放
     *
     * @param roomInfo
     */
    void start(QNLiveRoomInfo roomInfo);

    void stopPlay();

    /**
     * 角色变化
     *
     * @param roleType
     */
    void changeClientRole(ClientRoleType roleType);
}
