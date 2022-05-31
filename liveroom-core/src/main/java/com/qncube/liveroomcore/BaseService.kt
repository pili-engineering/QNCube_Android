package com.qncube.liveroomcore

import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser

open class BaseService : QNLiveService {

    protected var user: QNLiveUser? = null
    protected var roomInfo: QNLiveRoomInfo? = null
    protected var client: QNLiveRoomClient? = null

    /**
     * 进入回调
     *
     * @param user
     */
    override fun onRoomEnter(liveId: String, user: QNLiveUser) {
        this.user = user
    }

    /**
     * 加入回调
     *
     * @param roomInfo
     */
    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        this.roomInfo = roomInfo
    }

    /**
     * 离开回调
     */
    open override fun onRoomLeave() {
        user = null
    }

    /**
     * 销毁回调
     */
    open override fun onRoomClose() {

    }

    open override fun attachRoomClient(client: QNLiveRoomClient) {
        this.client = client
    }
}