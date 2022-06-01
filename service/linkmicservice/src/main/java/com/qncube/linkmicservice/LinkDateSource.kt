package com.qncube.linkmicservice

import com.alibaba.fastjson.util.ParameterizedTypeImpl
import com.nucube.http.OKHttpService
import com.qiniu.jsonutil.JsonUtils
import com.qncube.liveroomcore.Extension
import org.json.JSONObject

class LinkDateSource {

    suspend fun getMicList(liveId: String): List<QNMicLinker> {
        val p = ParameterizedTypeImpl(
            arrayOf(QNMicLinker::class.java),
            List::class.java,
            List::class.java
        )

        val resp: List<QNMicLinker> = OKHttpService.get(
            "/client/mic/room/list/${liveId}",
            null, null, p
        )
        return resp
    }

    suspend fun upMic(linker: QNMicLinker): TokenData {
        return OKHttpService.post("/client/mic/", JsonUtils.toJson(linker), TokenData::class.java)
    }

    suspend fun downMic(linker: QNMicLinker) {
        OKHttpService.delete("/client/mic/", JsonUtils.toJson(linker), Any::class.java)
    }

    suspend fun updateExt(linker: QNMicLinker, extension: Extension) {
        val jsonObj = JSONObject()
        jsonObj.put("live_id", linker.userRoomId)
        jsonObj.put("user_id", linker.user.userId)
        jsonObj.put("extends", extension)
        OKHttpService.put("/client/mic/room/", jsonObj.toString(), Any::class.java)
    }

    suspend fun switch(linker: QNMicLinker, isMic: Boolean, isOpen: Boolean) {
        val jsonObj = JSONObject()
        jsonObj.put("live_id", linker.userRoomId)
        jsonObj.put("user_id", linker.user.userId)
        jsonObj.put(
            "type", if (isMic) {
                "mic"
            } else {
                "camera"
            }
        )
        jsonObj.put(
            "status", if (isOpen) {
                "on"
            } else {
                "off"
            }
        )
        OKHttpService.put("/client/mic/switch", jsonObj.toString(), Any::class.java)
    }
}