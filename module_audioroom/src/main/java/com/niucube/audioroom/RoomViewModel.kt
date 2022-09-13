package com.niucube.audioroom

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hapi.happy_dialog.FinalDialogFragment
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
import com.niucube.qrtcroom.rtc.SimpleQNRTCListener
import com.niucube.rtm.RtmCallBack
import com.niucube.rtminvitation.Invitation
import com.niucube.rtminvitation.InvitationCallBack
import com.niucube.rtminvitation.InvitationManager
import com.niucube.rtminvitation.InvitationProcessor
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.bzuicomp.danmu.DanmuTrackManager
import com.qiniu.bzuicomp.gift.BigGiftManager
import com.qiniu.bzuicomp.gift.GiftMsg
import com.qiniu.bzuicomp.gift.GiftTrackManager
import com.qiniu.bzuicomp.pubchat.InputMsgReceiver
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RoomViewModel(application: Application, bundle: Bundle?) :
    BaseViewModel(application, bundle) {

    private val mHander = Handler(Looper.myLooper()!!)
    private val showTimeOutTime = 10 * 1000L
    private val timeOutDialog = CommonTipDialog.TipBuild().setContent("连接超时,请检查网络设置")
        .build()

    val mTotalUsersLivedata by lazy {
        MutableLiveData<List<RoomMember>>()
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

    //弹幕管理
    val mDanmuTrackManager = DanmuTrackManager()

    //公聊
    val mInputMsgReceiver = InputMsgReceiver()

    //欢迎消息
    val mWelComeReceiver = WelComeReceiver()

    //礼物轨道管理
    val mGiftTrackManager = GiftTrackManager()

    //大礼物队列
    val mBigGiftManager = BigGiftManager<GiftMsg>()

    //邀请信令
    val mInvitationProcessor = InvitationProcessor("audioroomupmic", object :
        InvitationCallBack {
        //收到上麦请求
        override fun onReceiveInvitation(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
            if (invitation.receiver == UserInfoManager.getUserId()) {
                showReceiveInvitation(invitation)
            }
        }

        //发起上麦请求超时
        override fun onInvitationTimeout(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
            Log.d("InvitationProcessor", "onInvitationTimeout ${invitation.receiver}")
        }

        //对方取消
        override fun onReceiveCanceled(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
        }

        //申请通过
        override fun onInviteeAccepted(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
            "${invitation.receiver}接受了你的上麦请求".asToast()
            sitDown()
        }

        //申请被拒绝
        override fun onInviteeRejected(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
            "${invitation.receiver}拒绝了你的上麦请求".asToast()
        }
    })

    private fun showReceiveInvitation(invitation: Invitation) {
        CommonTipDialog.TipBuild()
            .setTittle("上麦请求")
            .setContent(invitation.msg)
            .setListener(object : FinalDialogFragment.BaseDialogListener() {
                override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                    mInvitationProcessor.accept(invitation, object :
                        com.niucube.rtm.RtmCallBack {
                        override fun onSuccess() {}
                        override fun onFailure(code: kotlin.Int, msg: kotlin.String) {}
                    })
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

    //房间
    val mRtcRoom: LazySitMutableLiverRoom by lazy {
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

    //同步麦位
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

    init {
        InvitationManager.addInvitationProcessor(mInvitationProcessor)
    }

    //加入房间
    fun joinRoom(solutionType: String, roomId: String) {
        backGround {
            doWork {
                //业务服务器获取token
                val roomEntity = RetrofitManager.create(RoomService::class.java)
                    .joinRoom(JoinRoomEntity().apply {
                        this.roomId = roomId
                        type = solutionType
                    })
                //同步当前房间初始化麦位
                RoomAttributesManager.getRoomAllMicSeat(
                    solutionType, roomId
                ).let {
                    onGetRoomAllMicSeat(it)
                }
                //加入房间
                mRtcRoom.joinRoomAsAudience(roomEntity, null)

                //发送进入房间消息
                mWelComeReceiver.sendEnterMsg()
                if (roomEntity.isRoomHost()) {
                    sitDown()
                }
                //定时刷新房间信息
                refreshRoomJob()
                //开启心跳
                heartBeatJob()
            }
            catchError { e ->
                e.message?.asToast()
                finishedActivityCall?.invoke()
            }
        }
    }

    //发送上麦申请
    fun applySitDown() {
        mInvitationProcessor.invite(
            "用户${UserInfoManager.getUserInfo()?.nickname}发起上麦申请，是否同意？",
            RoomManager.mCurrentRoom?.hostId(),
            RoomManager.mCurrentRoom?.provideImGroupId(),
            -1,
            object : RtmCallBack {
                override fun onSuccess() {
                    "正在请求上麦，等待房主同意".asToast()
                }

                override fun onFailure(code: Int, msg: String) {
                    "$code $msg".asToast()
                }
            }
        )
    }

    //上麦
    fun sitDown() {
        backGround {
            showLoadingCall?.invoke(true)
            doWork {
                val userExt = UserExtension().apply {
                    uid = UserInfoManager.getUserId()
                    userExtProfile =
                        JsonUtils.toJson(UserInfoManager.getUserInfo()?.toUserExtProfile() ?: "")
                    userExtRoleType = ""
                }
                //业务服务器记录麦位
                RoomAttributesManager.sitDown(
                    RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                    JsonUtils.toJson(userExt),
                    listOf(Attribute(isOwnerOpenAudio, "1"))
                )
                //上麦
                mRtcRoom.sitDown(
                    userExt,
                    null,
                    AudioTrackParams()
                )
            }
            catchError { e ->
                e.message?.asToast()
                finishedActivityCall?.invoke()
            }
            onFinally {
                showLoadingCall?.invoke(false)
            }
        }
    }

    //下麦
    fun sitUp() {
        backGround {
            showLoadingCall?.invoke(true)
            doWork {
                mRtcRoom.sitUpAsAudience()
                RoomAttributesManager.sitUp(
                    RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                    RoomManager.mCurrentRoom?.provideMeId() ?: ""
                )
            }
            catchError {
                it.message?.asToast()
            }
            onFinally {
                showLoadingCall?.invoke(false)
            }
        }
    }

    private var isEnd = false
    fun endRoom() {
        mWelComeReceiver.sendQuitMsg("退出了房间")
        isEnd = true
        val room = (RoomManager.mCurrentRoom)?.asBaseRoomEntity() ?: return
        bgDefault {
            mRtcRoom.leaveRoom()
            mRtcRoom.closeRoom()
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
        InvitationManager.removeInvitationProcessor(mInvitationProcessor)
        if (RoomManager.mCurrentRoom?.isJoined == false) {
            mRtcRoom.closeRoom()
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

    private fun refreshRoomJob() {
        viewModelScope.launch {
            while (RoomManager.mCurrentRoom != null) {
                var delayTime = 5 * 1000L
                try {
                    RetrofitManager.create(RoomService::class.java).getRoomInfo(
                        (RoomManager.mCurrentRoom)!!.asBaseRoomEntity()?.roomInfo!!.type,
                        RoomManager.mCurrentRoom?.provideRoomId() ?: ""
                    ).let {
                        mTotalUsersLivedata.value = it.allUserList
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.message?.asToast()
                }
                delay(delayTime)
            }
        }
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
                        mTotalUsersLivedata.value = it.allUserList
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