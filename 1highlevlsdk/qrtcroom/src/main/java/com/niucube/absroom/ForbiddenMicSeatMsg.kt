package com.niucube.absroom

//禁言消息
class ForbiddenMicSeatMsg {
    var uid = ""
    var isForbidden = false
    var msg = ""

    constructor() {}
    constructor(uid: String, isForbidden: Boolean, msg: String) {
        this.uid = uid
        this.isForbidden = isForbidden
        this.msg = msg
    }
}