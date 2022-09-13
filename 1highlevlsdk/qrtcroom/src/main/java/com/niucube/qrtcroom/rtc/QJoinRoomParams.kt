package com.niucube.qrtcroom.rtc

import android.util.Base64
import org.json.JSONObject

class QJoinRoomParams(
    var token: String = "",
    var userData: String = "",
    var groupID: String = "",
    var pullUrl:String
) {
    var meId = ""
    var mRoomName = ""
    var mAppId = ""

    init {
        if (!token.isEmpty()) {
            val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
            val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
            val json: JSONObject = JSONObject(b64)
            mAppId = json.optString("appId")
            mRoomName = json.optString("roomName")
            meId = json.optString("userId")
        }
    }
}