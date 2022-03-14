package com.niucube.comproom


/**
 * 房间生命周期
 */
interface RoomLifecycleMonitor {


    //进入房间中
    open fun onRoomEntering(roomEntity: RoomEntity){}
    //加入房间
    open fun onRoomJoined(roomEntity: RoomEntity){}
    // 切换房间 比如上下滑动
    open fun onRoomLeft(roomEntity: RoomEntity?){}
    //关闭房间
    open fun onRoomClosed(roomEntity: RoomEntity?){}

}