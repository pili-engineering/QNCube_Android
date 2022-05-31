package com.qncube.liveroomcore

import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser

class QNLiveRoomContext(private val mClient: QNLiveRoomClient) {

    private val serviceMap = HashMap<Class<*>, Any>()
    private val mLifeCycleListener = ArrayList<QNRoomLifeCycleListener>()
    val mRoomScheduler = RoomScheduler()
    var roomInfo: QNLiveRoomInfo? = null

    init {
        mLifeCycleListener.add(mRoomScheduler)
    }

    fun <T : QNLiveService> registerService(serviceClass: Class<T>) {

        val classStr = serviceClass.name + "Impl"
        val classImpl = Class.forName(classStr)
        val constructor = classImpl.getConstructor()
        val obj = constructor.newInstance() as QNLiveService
        serviceMap[serviceClass] = obj
        mLifeCycleListener.add(obj)
        obj.attachRoomClient(mClient)

    }

    fun <T : QNLiveService> getService(serviceClass: Class<T>): T? {
        return serviceMap[serviceClass] as T?
    }

    /**
     * 添加房间生命周期状态监听
     *
     * @param lifeCycleListener
     */
    fun addRoomLifeCycleListener(lifeCycleListener: QNRoomLifeCycleListener) {
        mLifeCycleListener.add(lifeCycleListener)
    }

    //移除房间生命周期状态监听
    fun removeRoomLifeCycleListener(lifeCycleListener: QNRoomLifeCycleListener) {
        mLifeCycleListener.remove(lifeCycleListener)
    }


    fun enter(liveId: String, user: QNLiveUser) {
        mLifeCycleListener.forEach {
            it.onRoomEnter(liveId, user)
        }
    }

    fun leaveRoom() {
        mLifeCycleListener.forEach {
            it.onRoomLeave()
        }
        this.roomInfo = null
    }

    fun joinedRoom(roomInfo: QNLiveRoomInfo) {
        this.roomInfo = roomInfo
        mLifeCycleListener.forEach {
            it.onRoomJoined(roomInfo)
        }
    }

    fun close() {
        mLifeCycleListener.forEach {
            it.onRoomClose()
        }
        mLifeCycleListener.clear()
        serviceMap.clear()
    }

}