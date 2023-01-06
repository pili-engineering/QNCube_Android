package com.niucube.module.videowatch


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.hapi.ut.ViewUtil
import com.hipi.vm.activityVm
import com.niucube.absroom.RtcOperationCallback
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.niucube.module.videowatch.databinding.FragmentMicSeatBinding
import com.niucube.module.videowatch.databinding.MovieItemMicseatBinding
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.droid.rtc.QNTextureView
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.been.UserExtProfile
import com.qiniudemo.baseapp.been.hostId
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog

class MicSeatFragment : BaseFragment<FragmentMicSeatBinding>() {

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
            binding.tvSeatSize.text = "${roomVm.mRtcRoom.mMicSeats.size} 人通话中"
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
            binding.tvSeatSize.text = "${roomVm.mRtcRoom.mMicSeats.size} 人通话中"
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
                binding.ivCameraStatus.isSelected = !micSeat.isOwnerOpenVideo
            }
        }

        override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {
            if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                seatViews.get(0).convert(false, micSeat)
            } else {
                seatViews.get(1).convert(false, micSeat)
            }
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                binding.ivMicStatus.isSelected = !micSeat.isOpenAudio()
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
            binding.tvSeatSize.text = "${roomVm.mRtcRoom.mMicSeats.size} 人通话中"
        }

        override fun onKickOutFromMicSeat(seat: LazySitUserMicSeat, msg: String) {
            super.onKickOutFromMicSeat(seat, msg)
            "${seat.uid} 被管理员下麦".asToast()
            //  onUserSitUp(seat, false)
            if (seat.isMySeat(UserInfoManager.getUserId())) {
                roomVm.sitUp()
            }
        }

        override fun onKickOutFromRoom(userId: String, msg: String) {
            super.onKickOutFromRoom(userId, msg)

            if (userId == UserInfoManager.getUserId()) {
                "房主请你离开房间".asToast()
                if (roomVm.mRtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                    roomVm.sitUp()
                }
                requireActivity().finish()
            }
        }
    }

    var backCall = {}
    var goInviteOnlineUserPageCall = {}

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        bgDefault {
//            beautyDialog.loadRes(requireContext())
//        }

        binding.ivScale.setOnClickListener {
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

        binding.ivBeauty.setOnClickListener {

            // beautyDialog.show(childFragmentManager, "")
        }
        binding.ivCameraStatus.setOnClickListener {
            roomVm.mRtcRoom.muteLocalVideo(!binding.ivCameraStatus.isSelected)
        }
        binding.ivMicStatus.setOnClickListener {
            roomVm.mRtcRoom.muteLocalAudio(!binding.ivMicStatus.isSelected)
        }
        binding.ivSwitchCamera.setOnClickListener {
            roomVm.mRtcRoom.switchCamera()
        }
        binding.sitUp.setOnClickListener {
            if (roomVm.mRtcRoom.mClientRole != ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                backCall.invoke()
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
        seatViews.add(binding.leftSeat.apply {
            goInviteOnlineUserPageCall = this@MicSeatFragment.goInviteOnlineUserPageCall
        })
        seatViews.add(binding.rightSeat.apply {
            goInviteOnlineUserPageCall = this@MicSeatFragment.goInviteOnlineUserPageCall
        })
    }

    fun onPlayModeChanged(model: Int) {}

    override fun onDestroy() {
        roomVm.mRtcRoom.removeUserMicSeatListener(micSeatListener)
        super.onDestroy()
    }

}

class MicSeatsView : FrameLayout {
    val heightRatio = 1.28
    var goInviteOnlineUserPageCall = {}
    private lateinit var binding: MovieItemMicseatBinding

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        binding = MovieItemMicseatBinding.inflate(LayoutInflater.from(context), this, true)
        binding.hrItemMicSeat.heightRatio = heightRatio
    }

    fun convert(isAdd: Boolean, item: LazySitUserMicSeat?) {
        binding.ivJia.setOnClickListener {
            if (RoomManager.mCurrentRoom?.isRoomHost() == false) {
                goInviteOnlineUserPageCall.invoke()
            }
        }
        if (TextUtils.isEmpty(item?.uid)) {
            binding.llFooter.visibility = View.VISIBLE
            binding.flContent.visibility = View.GONE
        } else {
            binding.llFooter.visibility = View.GONE
            binding.flContent.visibility = View.VISIBLE
            item?.userExtension?.userExtProfile?.let {
                val userExtProfile = JsonUtils.parseObject(it, UserExtProfile::class.java)
                userExtProfile?.let {
                    Glide.with(context)
                        .load(it.avatar)
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(ViewUtil.dip2px(6f))))
                        .into(binding.ivMicSeatAvatar)
                    binding.tvMicNick.text = it.name
                }
            }
            binding.ivMicSeatAvatar.isVisible = !(item?.isOpenVideo() ?: false)
            binding.ivMicrophoneStatus.isSelected = item?.isOpenAudio() ?: false
        }
    }
}