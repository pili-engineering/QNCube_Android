package com.qiniudemo.module.interview.room

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleObserver
import com.bumptech.glide.Glide
import com.niucube.comp.mutabletrackroom.MutableTrackRoom
import com.niucube.rtcroom.mixstream.MixStreamManager
import com.niucube.absroom.seat.MicSeat
import com.niucube.absroom.seat.ScreenMicSeat
import com.niucube.absroom.seat.UserMicSeat
import com.qiniudemo.baseapp.widget.round.RoundFrameLayout
import com.qiniudemo.module.interview.R
import com.qiniudemo.module.interview.been.InterviewRoomModel
import kotlinx.android.synthetic.main.interview_interview_surfaceview.view.*

class InterviewSurfaceView : RoundFrameLayout, LifecycleObserver {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, mAttributeSet: AttributeSet?) : super(context, mAttributeSet) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.interview_interview_surfaceview, this, false)
        addView(view)
    }

    var mTargetSeat: MicSeat? = null
        private set

    var isTop = false
        private set

    fun setIsTop(top: Boolean) {
        isTop = top
        isEnabled = top
        if (top && tvNick.text.isNotEmpty()) {
            if (mTargetSeat != null) {
                tvNick.visibility = View.VISIBLE
            }
            // setRadius(8f, 8f, 8f, 8f)
        } else {
            tvNick.visibility = View.GONE
            // setRadius(0f, 0f, 0f, 0f)
        }
        // setStrokeWidthColor(1f, Color.parseColor("#00000000"));
    }

    fun setUserInfo(userInfo: InterviewRoomModel.RoomUser) {
        Glide.with(context)
            .load(userInfo.avatar)
            .into(ivAvatar)
        tvNick.text = userInfo.nickname
        if (isTop) {
            tvNick.visibility = View.VISIBLE
        }
    }

    fun setCover(coverRes: Int) {
        ivAvatar.setImageResource(coverRes)
    }


    fun onSeatDown(
        mutableTrackRoom: MutableTrackRoom,
        targetSeat: UserMicSeat
    ) {
        mTargetSeat = targetSeat
        mutableTrackRoom.setUserCameraWindowView(targetSeat.uid, qnSurfaceView)
        qnSurfaceView.visibility = View.VISIBLE
        ivAvatar.visibility = View.GONE
        if (isTop) {
            tvNick.visibility = View.VISIBLE
        }
        mutableTrackRoom.getMixStreamHelper().updateUserAudioMergeOptions(targetSeat.uid,
           true)
        val trackOp = MixStreamManager.MergeTrackOption()

        if (targetSeat.isMySeat()) {
            trackOp.mWidth = InterviewRoomVm.tack_width / 3
            trackOp.mHeight = InterviewRoomVm.track_heigt / 3
            trackOp.mX = InterviewRoomVm.tack_width / 3 * 2
            trackOp.mY = 0
            trackOp.mZ = 3
        } else {
            trackOp.mWidth = InterviewRoomVm.tack_width
            trackOp.mHeight = InterviewRoomVm.track_heigt
            trackOp.mX = 0
            trackOp.mY = 0
            trackOp.mZ = 1
        }
        mutableTrackRoom.getMixStreamHelper()
            .updateUserVideoMergeOptions(targetSeat.uid, trackOp)
    }

    fun onScreenSeatAdd(mutableTrackRoom: MutableTrackRoom, targetSeat: ScreenMicSeat) {
        mTargetSeat = targetSeat
        if (targetSeat.isMySeat()) {
            qnSurfaceView.visibility = View.GONE
            ivAvatar.visibility = View.VISIBLE
        } else {
            if (isTop) {
                tvNick.visibility = View.VISIBLE
            }
            qnSurfaceView.visibility = View.VISIBLE
            mutableTrackRoom.getScreenShareManager().setUserScreenWindowView(targetSeat.uid, qnSurfaceView)
        }
        val trackOp = MixStreamManager.MergeTrackOption()
        trackOp.mWidth = InterviewRoomVm.tack_width
        trackOp.mHeight = InterviewRoomVm.track_heigt
        trackOp.mX = 0
        trackOp.mY = 0
        trackOp.mZ = 2
        mutableTrackRoom.getMixStreamHelper()
            .updateUserScreenMergeOptions(targetSeat.uid, trackOp)
    }

    fun onScreenSeatRemoved(mutableTrackRoom: MutableTrackRoom, targetSeat: ScreenMicSeat) {
        mTargetSeat = null
        ivAvatar.visibility = View.VISIBLE
        qnSurfaceView.visibility = View.GONE
    }

    //下麦
    fun onSeatLeave(
        mutableTrackRoom: MutableTrackRoom,
        targetSeat: UserMicSeat
    ) {
        mTargetSeat = null
        ivAvatar.visibility = View.VISIBLE
        qnSurfaceView.visibility = View.GONE
    }

    //麦位变化
    fun onTrackStatusChange(mutableTrackRoom: MutableTrackRoom, targetSeat: UserMicSeat) {
        if (targetSeat.isOwnerOpenVideo) {
            qnSurfaceView.visibility = View.VISIBLE
        } else {
            qnSurfaceView.visibility = View.GONE
        }
    }

}