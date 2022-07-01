package com.niucube.comproom

import com.niucube.comproom.RoomEntity

/**
 * 观众端拉流播放器
 */
interface IAudiencePlayerView  {

    /**
     * 开始播放拉流地址
     * 1角色变跟为拉流端观众
     * 2观众角色进入房间
     */
    fun startAudiencePlay(roomEntity: RoomEntity)

    /**
     * 停止播放拉流地址
     * 1角色变跟为主播
     * 2用户角色房间离开销毁
     */
    fun stopAudiencePlay()

}