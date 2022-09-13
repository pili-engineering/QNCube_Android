package com.niucube.absroom

import com.niucube.absroom.seat.UserExtension

//用户角色加入消息
interface IAudienceJoinListener {
    fun onUserJoin(userExt: UserExtension)
    //用户角色离开 用户角色断线没有回调
    //用户角色离开 用户角色断线暂时没有回调 v5加
    fun onUserLeave(userExt: UserExtension)
}