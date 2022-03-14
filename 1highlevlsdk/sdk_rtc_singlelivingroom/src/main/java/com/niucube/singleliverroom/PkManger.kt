package com.niucube.singleliverroom

import com.niucube.absroom.RtcOperationCallback

class PkManger {

    //用户角色初次进入房间 需要业务服务器同步一下当前房间pk状态.
    fun userClientTypeSyncPkStatus(pkSession: PkSession) {

    }

    fun setPkListener(listener: PkListener) {

    }

    //pk开启  v4参数暂定
    fun startPk(
        pkSessionId: String,
        receiverUid: String,
        receiverRoomToken:String,
        pkExtension: String,
        callBack: RtcOperationCallback
    ) {

    }

    fun stopPk(code: Int, msg: String, callBack: RtcOperationCallback) {

    }

    //pk场次中 a房间群和b主播房间群都要收到的事件 如两边粉丝刷礼物的数量
    fun sendPkEvent(eventKey: String, value: String, callBack: RtcOperationCallback) {
    }
}