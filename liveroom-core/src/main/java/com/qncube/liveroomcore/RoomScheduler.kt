package com.qncube.liveroomcore

import com.qncube.liveroomcore.datasource.RoomDataSource
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser

class RoomScheduler : QNRoomLifeCycleListener {

    protected var user: QNLiveUser? = null
    protected var roomInfo: QNLiveRoomInfo? = null
    protected var client: QNLiveRoomClient? = null

    private var initRoomStatus = -100

    var roomStatusChange: (status: Int) -> Unit = {

    }

    private val roomDataSource = RoomDataSource()
    private val mHeartBeatJob = Scheduler(4000) {
        backGround {
            doWork {
                roomDataSource.heartbeat(roomInfo?.liveId ?: "")
                // val room = roomDataSource.refreshRoomInfo(roomInfo?.liveId ?: "")
//                if (room.liveStatus != roomInfo?.liveStatus
//                    && room.liveId == roomInfo?.liveId
//                ) {
//                    roomStatusChange.invoke(room.liveStatus)
//                }
                if(initRoomStatus<=0){
                    initRoomStatus
                }
            }
            catchError {

            }
        }
    }

    override fun onRoomEnter(liveId: String, user: QNLiveUser) {
        this.user = user
    }


    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        this.roomInfo = roomInfo
        mHeartBeatJob.start()
    }


    open override fun onRoomLeave() {
        user = null
        mHeartBeatJob.cancel()
        initRoomStatus = -100
    }


    open override fun onRoomClose() {
        mHeartBeatJob.cancel()
    }
}