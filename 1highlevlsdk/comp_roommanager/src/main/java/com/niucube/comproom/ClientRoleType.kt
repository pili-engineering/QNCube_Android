package com.niucube.comproom

enum class ClientRoleType(val role:Int) {
    /**
     * 主播角色
     * rtc房间里的表演者 有权限发流
     */
    CLIENT_ROLE_BROADCASTER(0),
    /**
     * 观众角色
     * rtc房间里听众 不表演只有权限收rtc房间轨道流
     *
     */
    CLIENT_ROLE_AUDIENCE(1),

    /**拉流角色
     * 使用 rtmp hls等直播协议拉房间里的合流的角色 不在rtc房间里
     * 需要房间合流转推后得到流
     * 拉流角色不在rtc房间里 延迟大但是费用低
     */
    CLIENT_ROLE_PULLER(-1)

}

