package com.qiniudemo.baseapp.been

import com.qiniu.bzcomp.user.UserInfo

class UserExtProfile {
    var avatar = ""
    var name = ""
}

fun UserInfo.toUserExtProfile(): UserExtProfile {
    val info = this
    return UserExtProfile().apply {
        avatar = info.avatar
        name = info.nickname
    }
}