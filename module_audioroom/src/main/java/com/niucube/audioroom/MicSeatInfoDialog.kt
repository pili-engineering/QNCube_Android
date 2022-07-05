package com.niucube.audioroom

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.bumptech.glide.Glide
import com.hipi.vm.activityVm
import com.niucube.basemutableroom.absroom.RtcOperationCallback
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.BaseDialogFragment
import com.qiniudemo.baseapp.been.UserExtProfile
import kotlinx.android.synthetic.main.aroom_seatinfo_dialog.*

class MicSeatInfoDialog : BaseDialogFragment() {

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
        roomVm.mRtcRoom?.mMicSeats?.forEach {
            if(it.uid == uid){
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
                    .into(ivAvatar)
                tvName.text = it.name
            }
        }

        if(uid == UserInfoManager.getUserId()){
            llOp.visibility  = View.GONE
        }else{
            llOp.visibility  = View.VISIBLE
        }
        tvForbiddenMic.text = if (seat!!.isForbiddenAudioByManager) {
            "解开禁麦"
        } else {
            "禁麦"
        }

        tvForbiddenCamera.text = if (seat!!.isForbiddenVideoByManager) {
            "解开禁视频"
        } else {
            "关视频"
        }

        tvForbiddenMic.setOnClickListener {

            roomVm.mRtcRoom?.forbiddenMicSeatAudio(uid, !seat!!.isForbiddenAudioByManager, "",
                object : RtcOperationCallback {
                    override fun onSuccess() {
                    }

                    override fun onFailure(errorCode: Int, msg: String) {
                    }
                })
            dismiss()
        }
        tvForbiddenCamera.setOnClickListener {
            roomVm.mRtcRoom?.forbiddenMicSeatVideo(uid, !seat!!.isForbiddenVideoByManager, "",
                object : RtcOperationCallback {
                    override fun onSuccess() {
                    }

                    override fun onFailure(errorCode: Int, msg: String) {
                    }
                })
            dismiss()
        }
        tvKick.setOnClickListener {
            roomVm.mRtcRoom?.kickOutFromMicSeat(uid, "", object : RtcOperationCallback {
                override fun onSuccess() {
                }

                override fun onFailure(errorCode: Int, msg: String) {
                }
            })
            dismiss()
        }
    }

    override fun getViewLayoutId(): Int {
        return R.layout.aroom_seatinfo_dialog
    }

}