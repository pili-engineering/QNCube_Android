package com.niucube.absroom

class UserMicSeatMsg<T> {
    var seat: T? = null
    var msg = ""

    constructor() {}
    constructor(seat: T, msg: String) {
        this.seat = seat
        this.msg = msg
    }
}