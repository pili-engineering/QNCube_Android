package com.niucube.comproom

enum class ClientRoleType(val role:Int) {
    /**
     * 主播角色
     */
    CLIENT_ROLE_BROADCASTER(0),
    /**
     * 用户角色
     */
    CLIENT_ROLE_AUDIENCE(1),

    //拉流角色
    CLIENT_ROLE_PULLER(-1)

}

