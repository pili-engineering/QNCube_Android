package com.niucube.module.amusement

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hipi.vm.BaseViewModel
import com.hipi.vm.bgDefault
import com.niucube.absroom.AudioTrackParams
import com.niucube.absroom.RtcOperationCallback
import com.niucube.absroom.RtcOperationException
import com.niucube.absroom.VideoTrackParams
import com.niucube.absroom.seat.UserExtension
import com.niucube.channelattributes.AttrRoom
import com.niucube.channelattributes.RoomAttributesManager
import com.niucube.comproom.RoomManager
import com.niucube.comproom.provideMeId
import com.niucube.lazysitmutableroom.LazySitMutableLiverRoom
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.droid.rtc.QNConnectionDisconnectedInfo
import com.qiniu.droid.rtc.QNConnectionState
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.been.*
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.RoomIdType
import com.qiniudemo.baseapp.service.RoomService
import com.qiniudemo.baseapp.widget.CommonTipDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.handleCoroutineException
import kotlinx.coroutines.launch

class RoomViewModel(application: Application, bundle: Bundle?) :
    BaseViewModel(application, bundle) {

    private val mHander = Handler(Looper.myLooper()!!)
    private val showTimeOutTime = 10 * 1000L
    private val timeOutDialog = CommonTipDialog.TipBuild().setContent("连接超时,请检查网络设置")
        .build()
    val mTotalUsersLivedata by lazy {
        MutableLiveData<Int>()
    }

    /**
     *  重新连接超时任务
     */
    private val timeOutRun = Runnable {
        getFragmentManagrCall?.invoke()?.let {
            timeOutDialog
                .show(it, "rtctimeout")
        }
    }

    //房间
    val mRtcRoom by lazy {
        //创建多轨道房间
        LazySitMutableLiverRoom(application).apply {
            addExtraQNRTCEngineEventListener(object : SimpleQNRTCListener {
                override fun onConnectionStateChanged(
                    p0: QNConnectionState,
                    p1: QNConnectionDisconnectedInfo?
                ) {
                    if (p0 == QNConnectionState.CONNECTING) {
                        "房间连接中..".asToast()
                    }
                    if (p0 == QNConnectionState.RECONNECTING) {
                        "房间重新连接中..".asToast()
                        startTimeOut()
                    }
                    if (p0 == QNConnectionState.RECONNECTED) {
                        "房间已经重新连接..".asToast()
                        cancelTimeOut()
                    }
                    if (p0 == QNConnectionState.CONNECTED) {
                        "房间连接成功..".asToast()
                        cancelTimeOut()
                    }
                }
            })
        }
    }

    //获得所有麦位
    private fun onGetRoomAllMicSeat(roomAttrs: AttrRoom) {
        val micSeats = ArrayList<LazySitUserMicSeat>()
        roomAttrs.mics.forEach {
            micSeats.add(LazySitUserMicSeat().apply {
                //用户ID
                uid = it.uid
                isOwnerOpenAudio =
                    it.attrs?.findValueOfKey("isOwnerOpenAudio") == "1"
                isOwnerOpenVideo =
                    it.attrs?.findValueOfKey("isOwnerOpenVideo") == "1"
                userExtension = JsonUtils.parseObject(
                    it.userExtension,
                    UserExtension::class.java
                )
                isMuteVideoByMe =
                    it.attrs.findValueOfKey("isMuteVideoByMe") == "1"
                isMuteAudioByMe =
                    it.attrs.findValueOfKey("isMuteAudioByMe") == "1"
            })
        }
        //初始化同步所有麦位
        mRtcRoom.userClientTypeSyncMicSeats(micSeats)
    }


    fun joinRoom(solutionType: String, roomId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val roomEntity = RetrofitManager.create(RoomService::class.java)
                    .joinRoom(JoinRoomEntity().apply {
                        this.roomId = roomId
                        type = solutionType
                    })
                mTotalUsersLivedata.value = (roomEntity.roomInfo?.totalUsers ?: "1").toInt()
                //同步当前房间初始化麦位
                RoomAttributesManager.getRoomAllMicSeat(
                    solutionType, roomId
                ).let {
                    onGetRoomAllMicSeat(it)
                }
                //加入房间
                mRtcRoom.joinRoomAsAudience(roomEntity, null)
                if (roomEntity.isRoomHost()) {
                    sitDown()
                }
                heartBeatJob()
            } catch (e: Exception) {
                e.printStackTrace()
                e.message?.asToast()
                finishedActivityCall?.invoke()
            }
        }
    }

    private var isSitDowning = false

    //上麦
    fun sitDown() {
        viewModelScope.launch(Dispatchers.Main) {
            if (isSitDowning) {
                return@launch
            }
            showLoadingCall?.invoke(true)
            isSitDowning = true
            try {
                val userExt = UserExtension().apply {
                    uid = UserInfoManager.getUserId()
                    userExtProfile =
                        JsonUtils.toJson(UserInfoManager.getUserInfo()?.toUserExtProfile() ?: "")
                    userExtRoleType = ""
                }
                //业务服务器保存麦位
                RoomAttributesManager.sitDown(
                    RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                    JsonUtils.toJson(userExt),
                    listOf(Attribute(isOwnerOpenAudio, "1"))
                )
                //上麦
                mRtcRoom.sitDown(
                    userExt,
                    VideoTrackParams(),
                    AudioTrackParams()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                e.message?.asToast()
                finishedActivityCall?.invoke()
            } finally {
                isSitDowning = false
                showLoadingCall?.invoke(false)
            }
        }
    }

    //下麦
    fun sitUp() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                mRtcRoom.sitUpAsAudience()
                RoomAttributesManager.sitUp(
                    RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                    RoomManager.mCurrentRoom?.provideMeId() ?: ""
                )
            }catch (e:Exception){
                e.printStackTrace()
                e.message?.asToast()
            }
        }
    }

    private var isEnd = false
    fun endRoom() {
        isEnd = true
        val room = (RoomManager.mCurrentRoom)?.asBaseRoomEntity() ?: return
        bgDefault {
            mRtcRoom.leaveRoom()
            mRtcRoom.closeRoom()
            bgDefault {
                RetrofitManager.create(RoomService::class.java)
                    .leaveRoom(
                        RoomIdType(
                            room.roomInfo!!.type,
                            room.provideRoomId() ?: ""
                        )
                    )
            }
        }
    }

    override fun onCleared() {
        if (!isEnd) {
            endRoom()
        }
    }

    private fun startTimeOut() {
        mHander.postDelayed(timeOutRun, showTimeOutTime)
    }

    private fun cancelTimeOut() {
        if (timeOutDialog.isVisible && timeOutDialog.isAdded) {
            timeOutDialog.dismiss()
        }
        mHander.removeCallbacks(timeOutRun)
    }

    private fun heartBeatJob() {
        viewModelScope.launch {
            while (RoomManager.mCurrentRoom != null) {
                var delayTime = 30 * 1000L
                try {
                    //定时心跳
                    val beat = RetrofitManager.create(RoomService::class.java)
                        .heartBeat(
                            (RoomManager.mCurrentRoom)!!.asBaseRoomEntity()?.roomInfo!!.type,
                            RoomManager.mCurrentRoom?.provideRoomId() ?: ""
                        )
                    delayTime = (beat.interval?.toLong() ?: 1)

                    RetrofitManager.create(RoomService::class.java).getRoomInfo(
                        (RoomManager.mCurrentRoom)!!.asBaseRoomEntity()?.roomInfo!!.type,
                        RoomManager.mCurrentRoom?.provideRoomId() ?: ""
                    ).let {
                        mTotalUsersLivedata.value = (it.roomInfo?.totalUsers ?: "1").toInt()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.message?.asToast()
                }
                delay(delayTime)
            }
        }
    }
}