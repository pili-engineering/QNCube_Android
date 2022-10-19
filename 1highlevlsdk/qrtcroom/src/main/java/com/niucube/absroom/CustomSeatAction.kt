package com.niucube.absroom

class CustomSeatAction {
    var uid = ""
    var key = ""
    var values = ""

    constructor() {}
    constructor(uid: String, key: String, values: String) {
        this.uid = uid
        this.key = key
        this.values = values
    }
}