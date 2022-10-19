package com.niucube.module.ktv

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hipi.vm.BaseViewModel
import com.hipi.vm.backGround
import com.hipi.vm.bgDefault
import com.niucube.absroom.AudioTrackParams
import com.niucube.absroom.seat.UserExtension
import com.niucube.channelattributes.AttrRoom
import com.niucube.channelattributes.RoomAttributesManager
import com.niucube.comproom.RoomManager
import com.niucube.comproom.provideMeId
import com.niucube.lazysitmutableroom.LazySitMutableLiverRoom
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.module.ktv.player.KTVPlayerKit
import com.niucube.module.ktv.playerlist.KTVPlaylistsManager
import com.niucube.qrtcroom.rtc.SimpleQNRTCListener
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.droid.rtc.QNConnectionDisconnectedInfo
import com.qiniu.droid.rtc.QNConnectionState
import com.qiniu.droid.rtc.QNMicrophoneAudioTrack
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.been.*
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.RoomIdType
import com.qiniudemo.baseapp.service.RoomService
import com.qiniudemo.baseapp.widget.CommonTipDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KTVRoomVm(application: Application, bundle: Bundle?) :
    BaseViewModel(application, bundle) {

    private val mHander = Handler(Looper.myLooper()!!)
    private val showTimeOutTime = 10 * 1000L
    private val timeOutDialog = CommonTipDialog.TipBuild().setContent("连接超时,请检查网络设置")
        .build()

    /**
     *  重新连接超时任务
     */
    private val timeOutRun = Runnable {
        getFragmentManagrCall?.invoke()?.let {
            timeOutDialog
                .show(it, "rtctimeout")
        }
    }

    val mKtvRoom by lazy {
        //创建多轨道房间
        val room = LazySitMutableLiverRoom(application)
        room.addExtraQNRTCEngineEventListener(object : SimpleQNRTCListener {
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
        room
    }

    //播放器
    val mKTVPlayerKit by lazy {
        KTVPlayerKit()
    }

    //歌单
    val mKTVPlaylistsManager by lazy {
        KTVPlaylistsManager(getAppContext())
    }

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
        //初始化麦位
        mKtvRoom.userClientTypeSyncMicSeats(micSeats)
    }

    fun joinRoom(solutionType: String, ktvRoomId: String) {
        backGround {
            doWork {
                //获取房间信息
                val roomEntity = RetrofitManager.create(RoomService::class.java)
                    .joinRoom(JoinRoomEntity().apply {
                        roomId = ktvRoomId
                        type = solutionType
                    })
                //同步当前房间初始化麦位
                RoomAttributesManager.getRoomAllMicSeat(
                    solutionType,
                    ktvRoomId
                ).let {
                    onGetRoomAllMicSeat(it)
                }
                //加入房间
                mKtvRoom.joinRoomAsAudience(roomEntity, null)
                //房主马上上麦
                if (roomEntity.isRoomHost()) {
                    sitDown()
                }else{
                    Log.d("KTVRoomVm","join not my room")
                }
                heartBeatJob()
            }
            catchError {
                it.message?.asToast()
                finishedActivityCall?.invoke()
            }
        }
    }

    //上麦
    fun sitDown() {
        backGround {
            doWork {
                val userExt = UserExtension().apply {
                    uid = UserInfoManager.getUserId()
                    userExtProfile =
                        JsonUtils.toJson(UserInfoManager.getUserInfo()?.toUserExtProfile() ?: "")
                    userExtRoleType = ""
                }
                //服务器保存麦位
                RoomAttributesManager.sitDown(
                    RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                    JsonUtils.toJson(userExt),
                    listOf(Attribute(isOwnerOpenAudio, "1"))
                )
                //上麦
                mKtvRoom.sitDown(userExt, null, AudioTrackParams())
                //绑定混音轨道
                mKTVPlayerKit.mMicrophoneAudioTrack =
                    mKtvRoom.getUserAudioTrackInfo(UserInfoManager.getUserId()) as QNMicrophoneAudioTrack?
            }
            catchError { e ->
                e.message?.asToast()
                finishedActivityCall?.invoke()
            }
        }
    }

    //下麦
    fun sitUp() {
        bgDefault {
            mKtvRoom.sitUpAsAudience()
            RoomAttributesManager.sitUp(
                RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                RoomManager.mCurrentRoom?.provideMeId() ?: ""
            )
        }
    }

    private var isEnd = false
    fun endRoom() {
        isEnd = true
        val room = (RoomManager.mCurrentRoom)?.asBaseRoomEntity() ?: return
        bgDefault {
            mKtvRoom.leaveRoom()
            mKtvRoom.closeRoom()
            RetrofitManager.create(RoomService::class.java)
                .leaveRoom(
                    RoomIdType(
                        room.roomInfo!!.type,
                        room.provideRoomId() ?: ""
                    )
                )
        }
    }

    override fun onCleared() {
        mKTVPlayerKit.releasePlayer()
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
                            (RoomManager.mCurrentRoom)!!.asBaseRoomEntity()
                                .roomInfo!!.type,
                            RoomManager.mCurrentRoom?.provideRoomId() ?: ""
                        )
                    delayTime = (beat.interval?.toLong() ?: 1)
                    //用户角色定时向服务器同步麦位
//                    if (mKtvRoom.mClientRole == ClientRoleType.CLIENT_ROLE_AUDIENCE) {
//                        RoomAttributesManager.getRoomAllMicSeat(
//                            (RoomManager.mCurrentRoom)!!.asBaseRoomEntity()
//                                .roomInfo!!.type,
//                            RoomManager.mCurrentRoom?.provideRoomId() ?: ""
//                        ).let {
//                            onGetRoomAllMicSeat(it)
//                        }
//                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.message?.asToast()
                }
                delay(delayTime)
            }
        }
    }

}