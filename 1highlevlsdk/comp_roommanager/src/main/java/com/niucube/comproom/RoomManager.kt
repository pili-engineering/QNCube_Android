package com.niucube.comproom

object RoomManager {

    /**
     * 当前房间信息
     */
    var mCurrentRoom: RoomEntity? = null
        private set

    private val mRoomLifecycleMonitors by lazy { ArrayList<RoomLifecycleMonitor>() }

    fun dispatchRoomEntering(roomEntity: RoomEntity) {
        mCurrentRoom = roomEntity
        mRoomLifecycleMonitors.forEach {
            it.onRoomEntering(roomEntity)
        }
    }

    fun dispatchRoomJoined(roomEntity: RoomEntity) {
        mCurrentRoom = roomEntity
        mCurrentRoom?.isJoined = true
        mRoomLifecycleMonitors.forEach {
            it.onRoomJoined(roomEntity)
        }
    }
//
//    fun dispatchRoomJoinFail(roomEntity: RoomEntity) {
//        mCurrentRoom?.isJoined = false
//        mRoomLifecycleMonitors.forEach {
//            it.onRoomJoinFail(roomEntity)
//        }
//    }

    fun dispatchRoomChannelJoin(joined: Boolean){
//        mRoomLifecycleMonitors.forEach {
//            it.onRoomChannelJoin(joined)
//        }
    }

    fun dispatchCloseRoom() {

        val tem = ArrayList<RoomLifecycleMonitor>().apply { addAll(mRoomLifecycleMonitors) }
        tem.forEach {
            it.onRoomClosed(mCurrentRoom)
        }
        mCurrentRoom?.isJoined = false
        mCurrentRoom = null
        mRoomLifecycleMonitors.clear()
    }

    fun dispatchRoomLeaving() {
        mRoomLifecycleMonitors.forEach {
            it.onRoomLeft(mCurrentRoom)
        }
        mCurrentRoom?.isJoined = false
        mCurrentRoom = null
    }

    fun addRoomLifecycleMonitor(lifecycleMonitor: RoomLifecycleMonitor) {
        mRoomLifecycleMonitors.add(lifecycleMonitor)
    }

    fun removeRoomLifecycleMonitor(lifecycleMonitor: RoomLifecycleMonitor) {
        mRoomLifecycleMonitors.remove(lifecycleMonitor)
    }
}