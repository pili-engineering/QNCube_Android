package com.qiniu.compui.trackview


interface TrackView<T> {

    var finishedCall :(()->Unit) ?
    /**
     * 是不是同一个轨道上的
     */
    fun showInSameTrack(trackMode: T):Boolean
    /**
     * 显示礼物
     */
    fun onNewModel(mode: T)
    /**
     * 是不是忙碌
     */
    fun isShow():Boolean
    /**
     * 退出直播间或者切换房间 清空
     */
    fun clear(isRoomChange:Boolean=false)
}