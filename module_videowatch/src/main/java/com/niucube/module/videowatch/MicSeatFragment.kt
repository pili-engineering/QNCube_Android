package com.niucube.module.videowatch


import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hapi.happy_dialog.FinalDialogFragment
import com.hapi.ut.ViewUtil
import com.hipi.vm.activityVm
import com.niucube.absroom.RtcOperationCallback
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.droid.rtc.QNTextureView
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.been.UserExtProfile
import com.qiniudemo.baseapp.been.hostId
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.qiniudemo.baseapp.widget.HeightRatioFrameLayout
import kotlinx.android.synthetic.main.fragment_mic_seat.*
import kotlinx.android.synthetic.main.movie_item_micseat.view.*


class MicSeatFragment : BaseFragment() {

    private val roomVm by activityVm<VideoRoomVm>()

    companion object {
        @JvmStatic
        fun newInstance() = MicSeatFragment()
    }

    private val seatViews = ArrayList<MicSeatsView>()
    //private val beautyDialog by lazy { StickerDialog() }
    private val micSeatListener = object : UserMicSeatListener {
        override fun onUserSitDown(micSeat: LazySitUserMicSeat) {
            var index = 0
            val seatView = if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                seatViews.get(0)
            } else {
                seatViews.get(1)
            }
            seatView.convert(true, micSeat)
            seatView.post {
                seatView.findViewById<ViewGroup>(R.id.hrItemMicSeat).addView(
                    QNTextureView(requireContext()).apply {
                        roomVm.mRtcRoom.setUserCameraWindowView(micSeat.uid, this)
                        //setRadius(ViewUtil.dip2px(6f).toFloat())
                    }, 0,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
            tvSeatSize.text = "${roomVm.mRtcRoom.mMicSeats.size} 人通话中"
        }

        override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {
            var index = 0
            val seatView = if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                seatViews.get(0)
            } else {
                seatViews.get(1)
            }
            seatView.convert(false, null)
            seatView.post {
                val parent = seatView.findViewById<ViewGroup>(R.id.hrItemMicSeat)
                if (parent.getChildAt(0) is QNTextureView) {
                    parent.removeViewAt(0)
                }
            }
            tvSeatSize.text = "${roomVm.mRtcRoom.mMicSeats.size} 人通话中"
            if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                "房主离开房间".asToast()
                roomVm.endRoom()
                CommonTipDialog.TipBuild()
                    .setContent("房间已经解散了～")
                    .isNeedCancelBtn(false)
                    .setListener(object : FinalDialogFragment.BaseDialogListener() {
                        override fun onDismiss(dialog: DialogFragment) {
                            super.onDismiss(dialog)
                            requireActivity().finish()
                        }
                    })
                    .buildDark()
                    .show(childFragmentManager, "")
            }
        }

        override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {
            if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                seatViews.get(0).convert(false, micSeat)
            } else {
                seatViews.get(1).convert(false, micSeat)
            }
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                ivCameraStatus.isSelected = !micSeat.isOwnerOpenVideo
            }
        }

        override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {
            if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                seatViews.get(0).convert(false, micSeat)
            } else {
                seatViews.get(1).convert(false, micSeat)
            }
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                ivMicStatus.isSelected = !micSeat.isOpenAudio()
            }
        }

        override fun onSyncMicSeats(seats: List<LazySitUserMicSeat>) {
            seats.forEachIndexed { index, micSeat ->
                if (index > 1) {
                    return
                }
                val seatView = seatViews.get(index)
                seatView.convert(true, micSeat)
                seatView.post {
                    seatView.findViewById<ViewGroup>(R.id.hrItemMicSeat).addView(
                        QNTextureView(requireContext()).apply {
                            roomVm.mRtcRoom.setUserCameraWindowView(micSeat.uid, this)
                        }, 0,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            }
            tvSeatSize.text = "${roomVm.mRtcRoom.mMicSeats.size} 人通话中"
        }

        override fun onKickOutFromMicSeat(seat: LazySitUserMicSeat, msg: String) {
            super.onKickOutFromMicSeat(seat, msg)
            "${seat.uid} 被管理员下麦".asToast()
            //  onUserSitUp(seat, false)
            if(seat.isMySeat(UserInfoManager.getUserId())){
                roomVm.sitUp()
            }
        }

        override fun onKickOutFromRoom(userId: String, msg: String) {
            super.onKickOutFromRoom(userId, msg)

            if (userId == UserInfoManager.getUserId()) {
                "房主请你离开房间".asToast()
                if(roomVm.mRtcRoom.mClientRole==ClientRoleType.CLIENT_ROLE_BROADCASTER){
                    roomVm.sitUp()
                }
                requireActivity().finish()
            }
        }
    }

    var backCall = {}
    var goInviteOnlineUserPageCall = {}

    @SuppressLint("ClickableViewAccessibility")
    override fun initViewData() {
//        bgDefault {
//            beautyDialog.loadRes(requireContext())
//        }

        ivScale.setOnClickListener {
            if (roomVm.mRtcRoom.mClientRole != ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                backCall?.invoke()
                return@setOnClickListener
            }
            if (RoomManager.mCurrentRoom?.isRoomHost() == false) {
                CommonTipDialog.TipBuild()
                    .setTittle("提示")
                    .setContent("你确定下麦么？")
                    .setListener(object : FinalDialogFragment.BaseDialogListener() {
                        override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                            super.onDialogPositiveClick(dialog, any)
                            roomVm.sitUp()
                            backCall.invoke()
                        }
                    }).buildNiuNiu().show(childFragmentManager, "")
            } else {
                backCall.invoke()
            }
        }

        ivBeauty.setOnClickListener {

           // beautyDialog.show(childFragmentManager, "")
        }
        ivCameraStatus.setOnClickListener {
            roomVm.mRtcRoom.muteLocalVideo(!ivCameraStatus.isSelected)
        }
        ivMicStatus.setOnClickListener {
            roomVm.mRtcRoom.muteLocalAudio(!ivMicStatus.isSelected)
        }
        ivSwitchCamera.setOnClickListener {
            roomVm.mRtcRoom.switchCamera()
        }
        sitUp.setOnClickListener {
            if (roomVm.mRtcRoom.mClientRole != ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                backCall?.invoke()
                return@setOnClickListener
            }
            if (RoomManager.mCurrentRoom?.isRoomHost() == false) {
                if (roomVm.mRtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                    roomVm.sitUp(object : RtcOperationCallback {
                        override fun onSuccess() {
                            requireActivity().finish()
                        }

                        override fun onFailure(errorCode: Int, msg: String) {
                            requireActivity().finish()
                        }
                    })
                }
            } else {
                CommonTipDialog.TipBuild()
                    .setTittle("确定离开房间吗?")
                    .setContent("房主离开后，房间自动解散，确定离开吗？")
                    .setListener(object : FinalDialogFragment.BaseDialogListener() {
                        override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                            super.onDialogPositiveClick(dialog, any)
                            roomVm.sitUp(object : RtcOperationCallback {
                                override fun onSuccess() {
                                    requireActivity().finish()
                                }

                                override fun onFailure(errorCode: Int, msg: String) {
                                    requireActivity().finish()
                                }
                            })

                        }
                    }).buildNiuNiu().show(childFragmentManager, "")
            }
        }
        roomVm.mRtcRoom.addUserMicSeatListener(micSeatListener)
        seatViews.add(leftSeat.apply {
            goInviteOnlineUserPageCall = this@MicSeatFragment.goInviteOnlineUserPageCall
        })
        seatViews.add(rightSeat.apply {
            goInviteOnlineUserPageCall = this@MicSeatFragment.goInviteOnlineUserPageCall
        })
    }

    fun onPlayModeChanged(model: Int) {}

    override fun onDestroy() {
        roomVm.mRtcRoom.removeUserMicSeatListener(micSeatListener)
        super.onDestroy()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_mic_seat
    }
}

class MicSeatsView : FrameLayout {
    val heightRatio = 1.28
    var goInviteOnlineUserPageCall = {}

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.movie_item_micseat, this, false)
        view.findViewById<HeightRatioFrameLayout>(R.id.hrItemMicSeat).heightRatio = heightRatio
        addView(view)
    }

    fun convert(isAdd: Boolean, item: LazySitUserMicSeat?) {

        ivJia.setOnClickListener {
            if (RoomManager.mCurrentRoom?.isRoomHost() == false) {
                goInviteOnlineUserPageCall.invoke()
            }
        }
        if (TextUtils.isEmpty(item?.uid)) {
            llFooter.visibility = View.VISIBLE
            flContent.visibility = View.GONE
        } else {
            llFooter.visibility = View.GONE
            flContent.visibility = View.VISIBLE
            item?.userExtension?.userExtProfile?.let {
                val userExtProfile = JsonUtils.parseObject(it, UserExtProfile::class.java)
                userExtProfile?.let {
                    Glide.with(context)
                        .load(it.avatar)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(ViewUtil.dip2px(6f))))
                        .into(ivMicSeatAvatar)
                    tvMicNick.text = it.name
                }
            }
            ivMicSeatAvatar.isVisible = !(item?.isOpenVideo() ?: false)
            ivMicrophoneStatus.isSelected = item?.isOpenAudio() ?: false
        }
    }
}