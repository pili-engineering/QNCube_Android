package com.qiniudemo.module.interview.room

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleObserver
import com.bumptech.glide.Glide
import com.niucube.mutabletrackroom.MutableTrackRoom
import com.niucube.absroom.seat.MicSeat
import com.niucube.absroom.seat.ScreenMicSeat
import com.niucube.absroom.seat.UserMicSeat
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.droid.rtc.QNTranscodingLiveStreamingTrack
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
        mutableTrackRoom.mixStreamManager.updateUserAudioMergeOptions(
            targetSeat.uid,
            QNTranscodingLiveStreamingTrack(), true
        )
        val trackOp = QNTranscodingLiveStreamingTrack()

        if (targetSeat.isMySeat(UserInfoManager.getUserId())) {
            trackOp.width = InterviewRoomVm.tack_width / 3
            trackOp.height = InterviewRoomVm.track_heigt / 3
            trackOp.x = InterviewRoomVm.tack_width / 3 * 2
            trackOp.y = 0
            trackOp.zOrder = 3
        } else {
            trackOp.width = InterviewRoomVm.tack_width
            trackOp.height = InterviewRoomVm.track_heigt
            trackOp.x = 0
            trackOp.y = 0
            trackOp.zOrder = 1
        }
        mutableTrackRoom.mixStreamManager
            .updateUserVideoMergeOptions(targetSeat.uid, trackOp,true)
    }

    fun onScreenSeatAdd(mutableTrackRoom: MutableTrackRoom, targetSeat: ScreenMicSeat) {
        mTargetSeat = targetSeat
        if (targetSeat.isMySeat(UserInfoManager.getUserId())) {
            qnSurfaceView.visibility = View.GONE
            ivAvatar.visibility = View.VISIBLE
        } else {
            if (isTop) {
                tvNick.visibility = View.VISIBLE
            }
            qnSurfaceView.visibility = View.VISIBLE
            mutableTrackRoom.screenShareManager.setUserScreenWindowView(targetSeat.uid, qnSurfaceView)
        }
        val trackOp = QNTranscodingLiveStreamingTrack()
        trackOp.width = InterviewRoomVm.tack_width
        trackOp.height = InterviewRoomVm.track_heigt
        trackOp.x = 0
        trackOp.y = 0
        trackOp.zOrder = 2
        mutableTrackRoom.mixStreamManager
            .updateUserScreenMergeOptions(targetSeat.uid, trackOp, true)
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