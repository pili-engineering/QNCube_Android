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
import com.niucube.basemutableroom.absroom.AudioTrackParams
import com.niucube.basemutableroom.absroom.seat.UserExtension
import com.niucube.channelattributes.AttrRoom
import com.niucube.channelattributes.RoomAttributesManager
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomManager
import com.niucube.comproom.provideMeId
import com.niucube.lazysitmutableroom.LazySitMutableLiverRoom
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.qnrtcsdk.SimpleQNRTCListener
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RoomViewModel(application: Application, bundle: Bundle?) :
    BaseViewModel(application, bundle) {

    private val mHander = Handler(Looper.myLooper()!!)
    private val showTimeOutTime = 10 * 1000L
    private val timeOutDialog = CommonTipDialog.TipBuild().setContent("????????????,?????????????????????")
        .build()

    val mTotalUsersLivedata by lazy {
        MutableLiveData<List<RoomMember>>()
    }

    /**
     *  ????????????????????????
     */
    private val timeOutRun = Runnable {
        getFragmentManagrCall?.invoke()?.let {
            timeOutDialog
                .show(it, "rtctimeout")
        }
    }

    //????????????
    val mDanmuTrackManager = DanmuTrackManager()

    //??????
    val mInputMsgReceiver = InputMsgReceiver()

    //????????????
    val mWelComeReceiver = WelComeReceiver()

    //??????????????????
    val mGiftTrackManager = GiftTrackManager()

    //???????????????
    val mBigGiftManager = BigGiftManager<GiftMsg>()

    //????????????
    val mInvitationProcessor = InvitationProcessor("audioroomupmic", object :
        InvitationCallBack {
        //??????????????????
        override fun onReceiveInvitation(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
            if (invitation.receiver == UserInfoManager.getUserId()) {
                showReceiveInvitation(invitation)
            }
        }

        //????????????????????????
        override fun onInvitationTimeout(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
            Log.d("InvitationProcessor", "onInvitationTimeout ${invitation.receiver}")
        }

        //????????????
        override fun onReceiveCanceled(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
        }

        //????????????
        override fun onInviteeAccepted(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
            "${invitation.receiver}???????????????????????????".asToast()
            sitDown()
        }

        //???????????????
        override fun onInviteeRejected(invitation: Invitation) {
            if (invitation.channelId != RoomManager.mCurrentRoom?.provideImGroupId()) {
                return
            }
            "${invitation.receiver}???????????????????????????".asToast()
        }
    })

    private fun showReceiveInvitation(invitation: Invitation) {
        CommonTipDialog.TipBuild()
            .setTittle("????????????")
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

    //??????
    val mRtcRoom: LazySitMutableLiverRoom by lazy {
        //?????????????????????
        LazySitMutableLiverRoom(application).apply {
            addExtraQNRTCEngineEventListener(object : SimpleQNRTCListener {
                override fun onConnectionStateChanged(
                    p0: QNConnectionState,
                    p1: QNConnectionDisconnectedInfo?
                ) {
                    if (p0 == QNConnectionState.CONNECTING) {
                        "???????????????..".asToast()
                    }
                    if (p0 == QNConnectionState.RECONNECTING) {
                        "?????????????????????..".asToast()
                        startTimeOut()
                    }
                    if (p0 == QNConnectionState.RECONNECTED) {
                        "????????????????????????..".asToast()
                        cancelTimeOut()
                    }
                    if (p0 == QNConnectionState.CONNECTED) {
                        "??????????????????..".asToast()
                        cancelTimeOut()
                    }
                }
            })
        }
    }

    //????????????
    private fun onGetRoomAllMicSeat(roomAttrs: AttrRoom) {
        val micSeats = ArrayList<LazySitUserMicSeat>()
        roomAttrs.mics.forEach {
            micSeats.add(LazySitUserMicSeat().apply {
                //??????ID
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

    //????????????
    fun joinRoom(solutionType: String, roomId: String) {
        backGround {
            doWork {
                //?????????????????????token
                val roomEntity = RetrofitManager.create(RoomService::class.java)
                    .joinRoom(JoinRoomEntity().apply {
                        this.roomId = roomId
                        type = solutionType
                    })
                //?????????????????????????????????
                RoomAttributesManager.getRoomAllMicSeat(
                    solutionType, roomId
                ).let {
                    onGetRoomAllMicSeat(it)
                }
                //????????????
                mRtcRoom.joinRoomAsAudience(roomEntity, null)

                //????????????????????????
                mWelComeReceiver.sendEnterMsg()
                if (roomEntity.isRoomHost()) {
                    sitDown()
                }
                //????????????????????????
                refreshRoomJob()
                //????????????
                heartBeatJob()
            }
            catchError { e ->
                e.message?.asToast()
                finishedActivityCall?.invoke()
            }
        }
    }

    //??????????????????
    fun applySitDown() {
        mInvitationProcessor.invite(
            "??????${UserInfoManager.getUserInfo()?.nickname}????????????????????????????????????",
            RoomManager.mCurrentRoom?.hostId(),
            RoomManager.mCurrentRoom?.provideImGroupId(),
            -1,
            object : RtmCallBack {
                override fun onSuccess() {
                    "???????????????????????????????????????".asToast()
                }

                override fun onFailure(code: Int, msg: String) {
                    "$code $msg".asToast()
                }
            }
        )
    }

    //??????
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
                //???????????????????????????
                RoomAttributesManager.sitDown(
                    RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                    JsonUtils.toJson(userExt),
                    listOf(Attribute(isOwnerOpenAudio, "1"))
                )
                //??????
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

    //??????
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
        mWelComeReceiver.sendQuitMsg("???????????????")
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
                    //????????????
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