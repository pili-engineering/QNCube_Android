package com.niucube.audioroom

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.bumptech.glide.Glide
import com.hipi.vm.activityVm
import com.niucube.absroom.RtcOperationCallback
import com.niucube.audioroom.databinding.AroomSeatinfoDialogBinding
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.BaseDialogFragment
import com.qiniudemo.baseapp.been.UserExtProfile

class MicSeatInfoDialog : BaseDialogFragment<AroomSeatinfoDialogBinding>() {

    private val roomVm by activityVm<RoomViewModel>()

    private var uid = ""

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    companion object {
        fun newInstance(uid: String): MicSeatInfoDialog {
            val d = MicSeatInfoDialog()
            val b = Bundle()
            b.putString("uid", uid)
            d.arguments = b
            return d
        }
    }

    override fun initViewData() {
        uid = arguments?.getString("uid") ?: ""
        var seat: LazySitUserMicSeat? = null
        roomVm.mRtcRoom.mMicSeats.forEach {
            if (it.uid == uid) {
                seat = it
                return@forEach
            }
        }
        if (seat == null) {
            dismiss()
            return
        }
        seat!!.userExtension?.userExtProfile?.let { it ->
            val userExtProfile = JsonUtils.parseObject(it, UserExtProfile::class.java)
            userExtProfile?.let {
                Glide.with(requireContext())
                    .load(it.avatar)
                    .into(binding.ivAvatar)
                binding.tvName.text = it.name
            }
        }

        if (uid == UserInfoManager.getUserId()) {
            binding.llOp.visibility = View.GONE
        } else {
            binding.llOp.visibility = View.VISIBLE
        }
        binding.tvForbiddenMic.text = if (seat!!.isForbiddenAudioByManager) {
            "解开禁麦"
        } else {
            "禁麦"
        }

        binding.tvForbiddenCamera.text = if (seat!!.isForbiddenVideoByManager) {
            "解开禁视频"
        } else {
            "关视频"
        }

        binding.tvForbiddenMic.setOnClickListener {

            roomVm.mRtcRoom.forbiddenMicSeatAudio(uid, !seat!!.isForbiddenAudioByManager, "",
                object : RtcOperationCallback {
                    override fun onSuccess() {
                    }

                    override fun onFailure(errorCode: Int, msg: String) {
                    }
                })
            dismiss()
        }
        binding.tvForbiddenCamera.setOnClickListener {
            roomVm.mRtcRoom.forbiddenMicSeatVideo(uid, !seat!!.isForbiddenVideoByManager, "",
                object : RtcOperationCallback {
                    override fun onSuccess() {
                    }

                    override fun onFailure(errorCode: Int, msg: String) {
                    }
                })
            dismiss()
        }
        binding.tvKick.setOnClickListener {
            roomVm.mRtcRoom.kickOutFromMicSeat(uid, "", object : RtcOperationCallback {
                override fun onSuccess() {
                }

                override fun onFailure(errorCode: Int, msg: String) {
                }
            })
            dismiss()
        }
    }


}