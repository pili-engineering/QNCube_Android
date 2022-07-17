package com.niucube.module.videowatch

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hapi.happy_dialog.FinalDialogFragment
import com.hapi.ut.helper.ActivityManager
import com.hipi.vm.BaseViewModel
import com.hipi.vm.backGround
import com.hipi.vm.bgDefault
import com.niucube.basemutableroom.absroom.AudioTrackParams
import com.niucube.basemutableroom.absroom.RtcOperationCallback
import com.niucube.basemutableroom.absroom.VideoTrackParams
import com.niucube.basemutableroom.absroom.seat.UserExtension
import com.niucube.channelattributes.AttrRoom
import com.niucube.channelattributes.RoomAttributesManager
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomManager
import com.niucube.comproom.provideMeId
import com.niucube.lazysitmutableroom.LazySitMutableLiverRoom
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.module.videowatch.core.MovieSignaler
import com.niucube.module.videowatch.core.RtcPubService
import com.niucube.module.videowatch.core.VideoRoomMixHelper
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.niucube.rtm.RtmCallBack
import com.niucube.rtminvitation.Invitation
import com.niucube.rtminvitation.InvitationCallBack
import com.niucube.rtminvitation.InvitationManager
import com.niucube.rtminvitation.InvitationProcessor
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.bzuicomp.pubchat.WelComeReceiver
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.droid.rtc.QNConnectionDisconnectedInfo
import com.qiniu.droid.rtc.QNConnectionState
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.been.*
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.RoomIdType
import com.qiniudemo.baseapp.service.RoomService
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CheckResult")
class VideoRoomVm(application: Application, bundle: Bundle?) :
    BaseViewModel(application, bundle) {

    private val mHander = Handler(Looper.myLooper()!!)
    private val showTimeOutTime = 10 * 1000L
    private val timeOutDialog = CommonTipDialog.TipBuild().setContent("连接超时,请检查网络设置")
        .buildDark()
    val roomInfoLiveData by lazy { MutableLiveData<BaseRoomEntity>() }
    private val mVideoMixHelper = VideoRoomMixHelper()
    // private val mVideoMixHelper = VideoMixHelper()

    /**
     *  重新连接超时任务
     */
    private val timeOutRun = Runnable {
        getFragmentManagrCall?.invoke()?.let {
            timeOutDialog
                .show(it, "rtctimeout")
        }
    }
    val mRtcPubService: RtcPubService = RtcPubService()
    val mMovieSignaler: MovieSignaler = MovieSignaler()

    val mWelComeReceiver = WelComeReceiver()
    val mRtcRoom by lazy {
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

    private fun showReceiveInvitation(invitation: Invitation) {
        CommonTipDialog.TipBuild()
            .setTittle("邀请提示")
            .setContent(invitation.msg)
            .setListener(object : FinalDialogFragment.BaseDialogListener() {
                override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                    mInvitationProcessor.accept(invitation, object : RtmCallBack {
                        override fun onSuccess() {}
                        override fun onFailure(code: Int, msg: String) {}
                    })
                    sitDown()
                }

                override fun onDialogNegativeClick(dialog: DialogFragment, any: Any) {
                    super.onDialogNegativeClick(dialog, any)
                    mInvitationProcessor.reject(invitation, object : RtmCallBack {
                        override fun onSuccess() {}
                        override fun onFailure(code: Int, msg: String) {}
                    })
                }
            })
            .buildNiuHappy().show(getFragmentManagrCall!!.invoke(), "")
    }

    //邀请信令
    val mInvitationProcessor = InvitationProcessor("watchMoviesTogether", object :
        InvitationCallBack {
        override fun onReceiveInvitation(invitation: Invitation) {
            if (mRtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                return
            }
            showReceiveInvitation(invitation)
        }

        override fun onInvitationTimeout(invitation: Invitation) {
            Log.d("InvitationProcessor", "onInvitationTimeout ${invitation.receiver}")
        }

        override fun onReceiveCanceled(invitation: Invitation) {}
        override fun onInviteeAccepted(invitation: Invitation) {
            "${invitation.receiver}接受了你的邀请".asToast()
        }

        override fun onInviteeRejected(invitation: Invitation) {
            "${invitation.receiver}拒绝了你的邀请".asToast()
        }
    })

    var solutionType = ""
    var roomId = ""
    var joinInvitationCode = ""

    init {
        solutionType = bundle?.getString("solutionType", "") ?: ""
        roomId = bundle?.getString("roomId", "") ?: ""
        joinInvitationCode = bundle?.getString("joinInvitationCode", "") ?: ""
        InvitationManager.addInvitationProcessor(mInvitationProcessor)
        RxPermissions(ActivityManager.get().currentActivity() as FragmentActivity)
            .request(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
            .subscribe {
                if (it) {
                    //进入房间
                    joinRoom()
                } else {
                    CommonTipDialog.TipBuild()
                        .setContent("请开启必要权限")
                        .setListener(object : FinalDialogFragment.BaseDialogListener() {
                            override fun onDismiss(dialog: DialogFragment) {
                                super.onDismiss(dialog)
                                finishedActivityCall?.invoke()
                            }
                        })
                        .build()
                        .show(getFragmentManagrCall!!.invoke(), "CommonTipDialog")
                }
            }
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
        mRtcRoom.userClientTypeSyncMicSeats(micSeats)
    }

    private fun joinRoom() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                //获取房间信息
                val roomEntity = RetrofitManager.create(RoomService::class.java)
                    .joinRoom(JoinRoomEntity().apply {
                        roomId = this@VideoRoomVm.roomId
                        type = solutionType
                        if (!joinInvitationCode.isEmpty()) {
                            params = listOf(Attribute("invitationCode", joinInvitationCode))
                        }
                    })
                roomId = roomEntity.provideRoomId()
                //同步当前房间初始化麦位
                RoomAttributesManager.getRoomAllMicSeat(
                    solutionType,
                    roomId
                ).let {
                    onGetRoomAllMicSeat(it)
                }
                if (roomEntity.isRoomHost()) {
                    //    mVideoMixHelper.attach(mRtcRoom, mRtcPubService)
                    mVideoMixHelper.attach(mRtcRoom)
                }
                //加入房间
                mRtcRoom.joinRoomAsAudience(roomEntity, null)
                mWelComeReceiver.sendEnterMsg("欢迎${UserInfoManager.getUserInfo()?.nickname}进入房间")
                //房主马上上麦
                if (roomEntity.isRoomHost()) {
                    mVideoMixHelper.start(mRtcRoom)
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

    //上麦
    fun sitDown() {
        if (mRtcRoom.mMicSeats.size > 1) {
            return
        }
        viewModelScope.launch(Dispatchers.Main) {
            try {
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
                mRtcRoom.sitDown(
                    userExt,
                    VideoTrackParams(),
                    AudioTrackParams()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                e.message?.asToast()
                finishedActivityCall?.invoke()
            }
        }
    }

    //下麦
    fun sitUp(call: RtcOperationCallback? = null) {
        backGround {
            doWork {
                mRtcRoom.sitUpAsAudience()
                RoomAttributesManager.sitUp(
                    RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                    RoomManager.mCurrentRoom?.provideMeId() ?: ""
                )
                call?.onSuccess()
            }
            catchError {
                call?.onFailure(-1,it.message?:"")
            }
        }
    }

    private var isEnd = false
    fun endRoom() {
        mWelComeReceiver.sendQuitMsg("${UserInfoManager.getUserInfo()?.nickname}退出了房间")
        InvitationManager.removeInvitationProcessor(mInvitationProcessor)
        isEnd = true
        val room = (RoomManager.mCurrentRoom)?.asBaseRoomEntity() ?: return
        mRtcRoom.closeRoom()
        bgDefault {
            mRtcRoom.leaveRoom()
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
        if (!isEnd) {
            endRoom()
        }
        mWelComeReceiver.onDestroy()
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

    suspend fun refreshRoomInfo(): BaseRoomEntity {
        val info = RetrofitManager.create(RoomService::class.java).getRoomInfo(
            (RoomManager.mCurrentRoom)!!.asBaseRoomEntity()
                .roomInfo!!.type,
            RoomManager.mCurrentRoom?.provideRoomId() ?: ""
        )
        roomInfoLiveData.value = info
        return info
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
                    refreshRoomInfo()
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.message?.asToast()
                }
                delay(delayTime)
            }
        }
    }

}