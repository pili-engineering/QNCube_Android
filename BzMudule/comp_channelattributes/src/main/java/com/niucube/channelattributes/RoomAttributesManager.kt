package com.niucube.channelattributes

import com.niucube.comproom.RoomManager
import com.niucube.rtm.*
import com.niucube.rtm.msg.RtmTextMsg
import com.qiniu.comp.network.NetBzException
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.been.Attribute
import com.qiniudemo.baseapp.been.BaseRoomEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.http.Field
import kotlin.coroutines.suspendCoroutine

/**
 * 房间属性管理
 */
object RoomAttributesManager {

    private val mRoomAttributesListeners = ArrayList<RoomAttributesListener>()
    private val mMicSeatAttributesListeners = ArrayList<MicSeatAttributesListener>()

    init {
        RtmManager.addRtmChannelListener(object : RtmMsgListener {
            override fun onNewMsg(msg: String, peerId: String): Boolean {
                when (msg.optAction()) {
                    "channelAttributes_change" -> {
                        val data = msg.optData()
                        val attr = JsonUtils.parseObject(data, RoomAttribute::class.java)
                        attr?.let { at ->
                            mRoomAttributesListeners.forEach {
                                it.onAttributesChange(at.roomId, at.key, at.value)
                            }
                        }
                        return true
                    }

                    "channelAttributes_clear" -> {
                        val data = msg.optData()
                        val attr = JsonUtils.parseObject(data, RoomAttribute::class.java)
                        attr?.let { at ->
                            mRoomAttributesListeners.forEach {
                                it.onAttributesClear(at.roomId, at.key)
                            }
                        }
                        return true
                    }

                    "seatAttributes_change" -> {
                        val data = msg.optData()
                        val attr = JsonUtils.parseObject(data, SeatAttribute::class.java)
                        attr?.let { at ->
                            mMicSeatAttributesListeners.forEach {
                                it.onAttributesChange(at.roomId, at.uid, at.key, at.value)
                            }
                        }
                        return true
                    }

                    "seatAttributes_clear" -> {
                        val data = msg.optData()
                        val attr = JsonUtils.parseObject(data, SeatAttribute::class.java)
                        attr?.let { at ->
                            mMicSeatAttributesListeners.forEach {
                                it.onAttributesClear(at.roomId, at.uid, at.key)
                            }
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    /**
     * 获得房间的所有属性和麦位属性
     */
    suspend fun getRoomAllMicSeat(type: String, roomId: String): AttrRoom {
        return RetrofitManager.create(AttributeService::class.java)
            .getRoomMicInfo(type, roomId)
    }

    suspend fun sitUp(
        roomId: String,
        uid: String
    ) {
        RetrofitManager.create(AttributeService::class.java)
            .downMic(roomId, (RoomManager.mCurrentRoom as BaseRoomEntity).roomInfo?.type ?: "", uid)
    }

    suspend fun sitDown(
        roomId: String? = null,
        userExtension: String? = null,
        attrs: List<Attribute?>? = null
    ) {
        RetrofitManager.create(AttributeService::class.java)
            .upMic(SitDownParams().apply {
                this.roomId = roomId
                this.type = (RoomManager.mCurrentRoom as BaseRoomEntity).roomInfo?.type ?: ""
                this.userExtension = userExtension
                this.attrs = attrs
            })
    }


    //注册房间属性监听
    fun addRoomAttributesListener(callback: RoomAttributesListener) {
        mRoomAttributesListeners.add(callback)
    }

    fun removeRoomAttributesListener(callback: RoomAttributesListener) {
        mRoomAttributesListeners.remove(callback)
    }

    //注册麦位监听
    fun addMicSeatAttributesListener(callback: MicSeatAttributesListener) {
        mMicSeatAttributesListeners.add(callback)
    }

    fun removeMicSeatAttributesListener(callback: MicSeatAttributesListener) {
        mMicSeatAttributesListeners.remove(callback)
    }

    //跟新房间key - values
    fun putRoomAttributes(
        roomId: String,
        key: String,
        values: String,
        isSendIm: Boolean,
        isDispatcher: Boolean,
        isSaveToServer: Boolean,
        callBack: AttributesCallBack<Unit>
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                if (isSaveToServer) {
                    RetrofitManager.create(AttributeService::class.java)
                        .updateRoomAttr(
                            UpdateRoomAttrParams().apply {
                                this.roomId = roomId
                                this.type =
                                    (RoomManager.mCurrentRoom as BaseRoomEntity).roomInfo?.type
                                        ?: ""
                                this.attrs = listOf(Attribute().apply {
                                    this.key = key
                                    this.value = values

                                })
                            }
                        )
                }
                if (isSendIm) {
                    RtmManager.rtmClient.sendChannelMsg(RtmTextMsg<RoomAttribute>(
                        "channelAttributes_change", RoomAttribute().apply {
                            this.roomId = roomId
                            this.key = key
                            this.value = values
                        }
                    ).toJsonString(),
                        RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                        isDispatcher)
                }
                callBack.onSuccess(Unit)
            } catch (e: RtmException) {
                e.printStackTrace()
                callBack.onFailure(
                    e.code, e.msg
                )
            } catch (e: NetBzException) {
                e.printStackTrace()
                callBack.onFailure(e.code, e.message ?: "")

            } catch (e: Exception) {
                e.printStackTrace()
                callBack.onFailure(1, e.message ?: "")
            }
        }
    }


    //查询房间key - values
    suspend fun getRoomAttributesByKeys(
        roomId: String,
        key: String,
        type:String=""
    ): Attribute {
        val atrs = RetrofitManager.create(AttributeService::class.java).getRoomAttr(
            roomId,
            if(type.isNotEmpty()){type}else{(RoomManager.mCurrentRoom as BaseRoomEntity).roomInfo?.type ?: ""}, key
        )
        if (atrs.attrs.isNullOrEmpty()) {
            return Attribute().apply {
                this.key = key
                this.value = ""
            }
        } else {
            return atrs.attrs!!.get(0)
        }
    }

    //跟新麦位key - values
    suspend fun putMicSeatAttributes(
        roomId: String,
        uid: String,
        key: String,
        values: String,
        isSendIm: Boolean,
        isSaveToServer: Boolean,
        callBack: AttributesCallBack<Void>
    ) {

        if (isSaveToServer) {
            RetrofitManager.create(AttributeService::class.java)
                .updateMicAttr(
                    UpdateMicAttrParams().apply {
                        this.roomId = roomId
                        this.uid = uid
                        this.type =
                            (RoomManager.mCurrentRoom as BaseRoomEntity).roomInfo?.type ?: ""
                        this.attrs = listOf(Attribute().apply {
                            this.key = key
                            this.value = value

                        })
                    }
                )
        }

        if (isSendIm) {
            RtmManager.rtmClient.sendChannelMsg(
                RtmTextMsg<RoomAttribute>(
                    "seatAttributes_change", RoomAttribute().apply {
                        this.roomId = roomId
                        this.key = key
                        this.value = values
                    }
                ).toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                true,
            )
        }
    }

    //查询麦位key - values
    suspend fun getMicSeatAttributesByKeys(
        roomId: String,
        uid: String,
        key: String,
    ): Attribute {
        val atrs = RetrofitManager.create(AttributeService::class.java).getMicAttr(
            roomId,
            (RoomManager.mCurrentRoom as BaseRoomEntity).roomInfo?.type ?: "", uid, key
        )
        if (atrs.attrs.isNullOrEmpty()) {
            return Attribute().apply {
                this.key = key
                this.value = ""
            }
        } else {
            return atrs.attrs!!.get(0)
        }
    }


    /**
     * 如果该场景用户端进入需要初始化UI
     *
     */


    //保存麦上用户摄像头 内置方法不发im通知 key:cameraStatus value:0 / 1
    fun saveCameraStatus(
        roomId: String,
        isOpen: String,
        callBack: AttributesCallBack<Void>
    ) {

    }

    //保存麦上用户麦克风 内置方法不发im通知  key:microphoneStatus value:0 / 1
    fun saveMicrophoneStatus(
        roomId: String,
        isOpen: String,
        callBack: AttributesCallBack<Void>
    ) {

    }

}