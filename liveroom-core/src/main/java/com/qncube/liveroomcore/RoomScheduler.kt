package com.qncube.liveroomcore

import com.qncube.liveroomcore.datasource.RoomDataSource
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser

open class RoomScheduler : QNRoomLifeCycleListener {

    protected var user: QNLiveUser? = null
    protected var roomInfo: QNLiveRoomInfo? = null
    protected var client: QNLiveRoomClient? = null
    private var roomStatus = 0
    private var anchorStatus = 1

    var roomStatusChange: (status: LiveStatus) -> Unit = {}

    private val roomDataSource = RoomDataSource()
    private val mHeartBeatJob = Scheduler(8000) {
        if (roomInfo == null) {
            return@Scheduler
        }
        backGround {
            doWork {
                val res = roomDataSource.heartbeat(roomInfo?.liveId ?: "")
                val room = roomDataSource.refreshRoomInfo(roomInfo?.liveId ?: "")

                if (res.liveStatus != roomStatus) {
                    roomStatus = res.liveStatus
                    roomStatusChange.invoke(roomStatus.roomStatusToLiveStatus())
                }
                if (anchorStatus != room.anchorStatus) {
                    anchorStatus = room.anchorStatus
                    roomStatusChange.invoke(anchorStatus.anchorStatusToLiveStatus())
                }
                QNLiveLogUtil.LogE("res.liveStatus ${res.liveStatus}   room.anchorStatus ${room.anchorStatus} ")
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
        roomStatus = roomInfo.liveStatus
        anchorStatus = roomInfo.anchorStatus
        mHeartBeatJob.start()
    }


    open override fun onRoomLeave() {
        user = null
        mHeartBeatJob.cancel()

    }


    open override fun onRoomClose() {
        mHeartBeatJob.cancel()
    }
}