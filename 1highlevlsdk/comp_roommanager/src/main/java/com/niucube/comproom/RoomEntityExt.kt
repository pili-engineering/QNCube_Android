package com.niucube.comproom

import android.util.Base64
import org.json.JSONObject

fun com.niucube.comproom.RoomEntity.provideMeId(): String {
    val token = this.provideRoomToken()
    val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
    val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
    val json: JSONObject = JSONObject(b64)
    return json.optString("userId") ?: ""
}