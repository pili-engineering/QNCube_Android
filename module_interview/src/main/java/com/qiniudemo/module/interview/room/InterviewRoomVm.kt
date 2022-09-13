package com.qiniudemo.module.interview.room

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hapi.ut.AppCache
import com.hipi.vm.BaseViewModel
import com.hipi.vm.bgDefault
import com.hipi.vm.vmScopeBg
import com.qiniu.bzuicomp.pubchat.*
import com.qiniu.comp.network.RetrofitManager
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.mutabletrackroom.MutableTrackRoom
import com.niucube.qrtcroom.rtc.SimpleQNRTCListener
import com.qiniu.droid.rtc.*
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.qiniudemo.module.interview.InterviewService
import com.qiniudemo.module.interview.been.InterviewRoomModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InterviewRoomVm(val app: Application, bundle: Bundle?) :
    BaseViewModel(app, bundle) {

    companion object {
        val tack_width = 480
        val track_heigt = 640
    }

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

    private fun startTimeOut() {
        mHander.postDelayed(timeOutRun, showTimeOutTime)
    }

    private fun cancelTimeOut() {
        if (timeOutDialog.isVisible && timeOutDialog.isAdded) {
            timeOutDialog.dismiss()
        }
        mHander.removeCallbacks(timeOutRun)
    }

    override fun onCleared() {
        super.onCleared()
        RoomManager.removeRoomLifecycleMonitor(roomMonitor)
        bgDefault {
            try {
                mInterviewRoom.leaveRoom()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            mInterviewRoom.closeRoom()
        }
        cancelTimeOut()
    }

    /**
     * 面试房间引擎
     */
    val mInterviewRoom by lazy {
        //创建多轨道房间
        val room = MutableTrackRoom(AppCache.getContext())
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
        //设置混流画布参数
        room
    }

    //混流工具
    private val mTrackMixStreamKit by lazy { mInterviewRoom.mixStreamManager }

    //房间生命周期监听
    private val roomMonitor = object : RoomLifecycleMonitor {
        //加入成功
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            //房主负责混流
            if ((roomEntity as InterviewRoomModel?)?.isRoomOwner == true) {
                mTrackMixStreamKit.startMixStreamJob(QNTranscodingLiveStreamingConfig().apply {
                    width = tack_width
                    height = track_heigt
                    bitrate = 1600
                    videoFrameRate = 15
                })
            }
        }
    }

    init {
        //初始化监听
        RoomManager.addRoomLifecycleMonitor(roomMonitor)
    }

    //公聊监听
    val mInputMsgReceiver = InputMsgReceiver()

    //自定义欢迎消息
    val mWelComeReceiver = WelComeReceiver()

    /**
     * 显示结束
     */
    val showLeaveInterviewLiveData by lazy { MutableLiveData<Boolean>() }

    //业务心跳
    private fun heartBeatJob() {
        viewModelScope.launch {
            while (RoomManager.mCurrentRoom != null) {
                var delayTime = 30 * 1000L
                try {
                    val beat = RetrofitManager.create(InterviewService::class.java)
                        .heartBeat(RoomManager.mCurrentRoom?.provideRoomId() ?: "")
                    delayTime = beat.interval.toLong() * 1000
                    showLeaveInterviewLiveData.value = beat.options.isShowLeaveInterview
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(delayTime)
            }
        }
    }

    //进入房间
    fun enterRoom(interviewId: String) {
        vmScopeBg {
            showLoadingCall?.invoke(true)
            doWork {
                //获取房间信息
                val token = RetrofitManager.create(InterviewService::class.java)
                    .joinInterview(interviewId)
                //主播模式
                mInterviewRoom.suspendSetClientRole(ClientRoleType.CLIENT_ROLE_BROADCASTER)
                //开启视频
                mInterviewRoom.enableVideo(object : QNPublishResultCallback {
                    override fun onPublished() {}
                    override fun onError(p0: Int, p1: String?) {}
                })
                //开启音频
                mInterviewRoom.enableAudio(object : QNPublishResultCallback {
                    override fun onPublished() {}
                    override fun onError(p0: Int, p1: String?) {}
                })
                //加入房间
                mInterviewRoom.joinRoom(token, null)
                heartBeatJob()
            }
            catchError {
                it.message?.asToast()
                finishedActivityCall?.invoke()
            }
            onFinally {
                showLoadingCall?.invoke(false)
            }
        }
    }

    fun levelInterviewRoom() {
        val roomEntity = RoomManager.mCurrentRoom as InterviewRoomModel?
        roomEntity?.let {
            bgDefault {
                RetrofitManager.create(InterviewService::class.java)
                    .leavelInterview(roomEntity.provideRoomId())
            }
        }
        finishedActivityCall?.invoke()
    }

    fun endRoom() {
        val roomEntity = RoomManager.mCurrentRoom as InterviewRoomModel?
        roomEntity?.let {
            bgDefault {
                RetrofitManager.create(InterviewService::class.java)
                    .endInterview(roomEntity.provideRoomId())
            }
        }
        finishedActivityCall?.invoke()
    }
}