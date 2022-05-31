package com.qncube.liveroomcore.datasource

import com.alibaba.fastjson.util.ParameterizedTypeImpl
import com.nucube.http.OKHttpService
import com.nucube.http.PageData
import com.qiniu.jsonutil.JsonUtils
import com.qncube.liveroomcore.Extension
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.backGround
import com.qncube.liveroomcore.getCode
import com.qncube.liveroomcore.mode.QNCreateRoomParam
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import org.json.JSONObject

class RoomDataSource {

    /**
     * 刷新房间信息
     */
    suspend fun refreshRoomInfo(liveId: String): QNLiveRoomInfo {
        return OKHttpService.get(
            "/client/live/room/info/${liveId}",
            null,
            QNLiveRoomInfo::class.java
        )
    }

    fun refreshRoomInfo(liveId: String, callBack: QNLiveCallBack<QNLiveRoomInfo>?) {
        backGround {
            doWork {
                val resp = refreshRoomInfo(liveId)
                callBack?.onSuccess(resp)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    suspend fun listRoom(pageNumber: Int, pageSize: Int): PageData<QNLiveRoomInfo> {
        val p = ParameterizedTypeImpl(
            arrayOf(QNLiveRoomInfo::class.java),
            PageData::class.java,
            PageData::class.java
        )
        val date: PageData<QNLiveRoomInfo> =
            OKHttpService.get("/client/live/room/list", HashMap<String, String>().apply {
                put("page_num", pageNumber.toString())
                put("page_size", pageSize.toString())
            }, null, p)
        return date
    }

    fun listRoom(pageNumber: Int, pageSize: Int, call: QNLiveCallBack<PageData<QNLiveRoomInfo>>?) {
        backGround {
            doWork {

                val resp = listRoom(pageNumber, pageSize)
                call?.onSuccess(resp)
            }
            catchError {
                call?.onError(it.getCode(), it.message)
            }
        }
    }

    suspend fun createRoom(param: QNCreateRoomParam): QNLiveRoomInfo {
        return OKHttpService.post(
            "/client/live/room/instance",
            JsonUtils.toJson(param),
            QNLiveRoomInfo::class.java
        )
    }

    fun createRoom(param: QNCreateRoomParam, callBack: QNLiveCallBack<QNLiveRoomInfo>?) {
        backGround {
            doWork {
                val room = createRoom(param)
                callBack?.onSuccess(room)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    fun deleteRoom(liveId: String, call: QNLiveCallBack<Void>?) {
        backGround {
            doWork {
                OKHttpService.delete("/live/room/instance/${liveId}", "{}", Any::class.java)
                call?.onSuccess(null)
            }
            catchError {
                call?.onError(it.getCode(), it.message)
            }
        }
    }

    suspend fun pubRoom(liveId: String): QNLiveRoomInfo {
        return OKHttpService.put("/client/live/room/${liveId}", "{}", QNLiveRoomInfo::class.java)
    }

    suspend fun joinRoom(liveId: String): QNLiveRoomInfo {
        return OKHttpService.put(
            "/client/live/room/user/${liveId}",
            "{}",
            QNLiveRoomInfo::class.java
        )
    }

    suspend fun leaveRoom(liveId: String) {
        OKHttpService.delete("/client/live/room/user/${liveId}", "{}", Any::class.java)
    }

    /**
     * 跟新直播扩展信息
     * @param extension
     */
    suspend fun updateRoomExtension(liveId: String, extension: Extension) {

        val json = JSONObject()
        json.put("live_id", liveId)
        json.put("extends", extension)

        OKHttpService.put(
            "/client/live/room/extends",
            json.toString(),
            Any::class.java
        )
    }

    suspend fun heartbeat(liveId: String) {
        OKHttpService.get("/live/room/heartbeat/${liveId}", null, Any::class.java)
    }
}