package com.niucube.overhaul

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.hipi.vm.BaseViewModel
import com.hipi.vm.backGround
import com.niucube.basemutableroom.absroom.VideoTrackParams
import com.niucube.basemutableroom.absroom.seat.UserExtension
import com.niucube.overhaul.mode.OverhaulRoom
import com.niucube.comproom.*
import com.qiniu.bzuicomp.pubchat.WelComeReceiver
import com.niucube.comp.mutabletrackroom.MutableTrackRoom
import com.qiniu.comp.network.RetrofitManager
import com.niucube.basemutableroom.mixstream.MixStreamManager
import com.qiniu.droid.whiteboard.QNWhiteBoard
import com.qiniu.droid.whiteboard.model.JoinConfig
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.qiniu.droid.rtc.*
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OverhaulVm(application: Application, bundle: Bundle?) :
    BaseViewModel(application, bundle) {

    var overhaulRoomEntity: OverhaulRoom? = null
        private set

    companion object {
        var videoWidth = 540
        var videoHeight = 960
    }

    private val mHander = Handler(Looper.myLooper()!!)
    private val showTimeOutTime = 10 * 1000L
    private val timeOutDialog = CommonTipDialog.TipBuild().setContent("连接超时,请检查网络设置")
        .build()

    // val mInputMsgReceiver = InputMsgReceiver()
    val mWelComeReceiver = WelComeReceiver()

    /**
     *  重新连接超时任务
     */
    private val timeOutRun = Runnable {
        getFragmentManagrCall?.invoke()?.let {
            timeOutDialog
                .show(it, "rtctimeout")
        }
    }

    private var isEnd = false
    override fun onCleared() {
        super.onCleared()
        if (!isEnd) {
            endRoom()
        }
        RoomManager.removeRoomLifecycleMonitor(roomMonitor)
        cancelTimeOut()

    }

    val mMutableTrackRoom by lazy {
        //创建多轨道房间
        val room = MutableTrackRoom(application)
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

        //设置混流背景
        room.getMixStreamHelper().setMixParams(
            MixStreamManager.MixStreamParams(
                videoWidth,
                videoHeight,
                3420,
                15,
                null
            )
        )
        room
    }

    private val roomMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            if (overhaulRoomEntity?.role == OverhaulRole.STAFF.role) {
                mMutableTrackRoom.getMixStreamHelper().startMixStreamJob()
            }
        }
    }

    init {
        RoomManager.addRoomLifecycleMonitor(roomMonitor)
    }

    fun enterRoom(overhaulRoomEntity: OverhaulRoom) {
        this.overhaulRoomEntity = overhaulRoomEntity
        val ext = UserExtension().apply {
            uid = overhaulRoomEntity.provideMeId()
            userExtRoleType = overhaulRoomEntity.role // 专家/学生/检修员
        }

        backGround {
            doWork {
                when (overhaulRoomEntity.role) {
                    OverhaulRole.PROFESSOR.role -> {
                        mMutableTrackRoom.suspendSetClientRole(ClientRoleType.CLIENT_ROLE_BROADCASTER)
                        mMutableTrackRoom.enableAudio(object : QNPublishResultCallback {
                            override fun onPublished() {}
                            override fun onError(p0: Int, p1: String?) {}
                        })
                    }
                    OverhaulRole.STAFF.role -> {
                        mMutableTrackRoom.suspendSetClientRole(ClientRoleType.CLIENT_ROLE_BROADCASTER)
                        //检修员开启视频和音频
                        mMutableTrackRoom.enableAudio(object : QNPublishResultCallback {
                            override fun onPublished() {}
                            override fun onError(p0: Int, p1: String?) {}
                        })
                        mMutableTrackRoom.setCameraVideoTrackParams(VideoTrackParams().apply {
                            width = videoWidth
                            height = videoHeight
                        })
                        mMutableTrackRoom.enableVideo(object : QNPublishResultCallback {
                            override fun onPublished() {}
                            override fun onError(p0: Int, p1: String?) {}
                        })
                    }
                    //
                    OverhaulRole.STUDENT.role -> {
                        if (overhaulRoomEntity.isStudentJoinRtc == true) {
                            mMutableTrackRoom.suspendSetClientRole(ClientRoleType.CLIENT_ROLE_AUDIENCE)
                        } else {
                            mMutableTrackRoom.suspendSetClientRole(ClientRoleType.CLIENT_ROLE_PULLER)
                        }
                    }
                }
                mMutableTrackRoom.joinRoom(
                    overhaulRoomEntity,
                    ext
                )
                //加入白板房间
                QNWhiteBoard.joinRoom(JoinConfig(overhaulRoomEntity.roomToken).apply {
                    widthHeightThan = videoWidth / videoHeight.toDouble()
                })
                heartBeatJob()
            }
            catchError {
                it.message?.asToast()
                finishedActivityCall?.invoke()
            }
        }

    }

    fun endRoom(callback: () -> Unit = {}) {
        isEnd = true
        QNWhiteBoard.leaveRoom()
        showLoadingCall?.invoke(true)
        viewModelScope.launch(Dispatchers.Main) {
            try {
                mMutableTrackRoom.leaveRoom()
                val roomEntity = RoomManager.mCurrentRoom as OverhaulRoom?
                RetrofitManager.create(OverhaulService::class.java)
                    .leaveRoom(roomEntity?.provideRoomId() ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mMutableTrackRoom.closeRoom()
                showLoadingCall?.invoke(false)
                callback.invoke()
            }
        }
    }

    suspend fun refreshRoomInfo() {
        val info = RetrofitManager.create(OverhaulService::class.java)
            .getRoomInfo(overhaulRoomEntity!!.provideRoomId())
        overhaulRoomEntity!!.roomInfo.status = info.roomInfo.status
        overhaulRoomEntity!!.allUserList = info.allUserList
        if (overhaulRoomEntity!!.roomInfo.status == 0) {
            endRoom {
                finishedActivityCall?.invoke()
            }
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
                    refreshRoomInfo()
                    val beat = RetrofitManager.create(OverhaulService::class.java)
                        .heartBeat(RoomManager.mCurrentRoom?.provideRoomId() ?: "")
                    delayTime = beat.interval.toLong() * 1000
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(delayTime)
            }
        }
    }
}