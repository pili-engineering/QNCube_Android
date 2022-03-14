package com.qiniudemo.module.interview.room

import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import com.hipi.vm.activityVm
import com.qiniu.bzuicomp.bottominput.RoomInputDialog
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.module.interview.R
import com.qiniudemo.module.interview.been.InterviewRoomModel
import kotlinx.android.synthetic.main.interview_fragment_room_cover.*


/**
 * 面试覆盖层
 */
class RoomCover : BaseFragment() {

    /**
     * activitity vm
     */
    private val mInterviewRoomVm by activityVm<InterviewRoomVm>()

    /**
     * 面试房间引擎
     */
    private val mInterviewRoom by lazy {
        mInterviewRoomVm.mInterviewRoom
    }
    private val mInputMsgReceiver  by lazy {
        mInterviewRoomVm.mInputMsgReceiver
    }
    private val mWelComeReceiver  by lazy {
        mInterviewRoomVm.mWelComeReceiver
    }

    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            clCoverContainer.visibility = View.VISIBLE
            roomTittle.text = (roomEntity as InterviewRoomModel).interview.title
            mWelComeReceiver.sendEnterMsg()
        }

        override fun onRoomEntering(roomEntity: RoomEntity) {
            super.onRoomEntering(roomEntity)
            if ((RoomManager.mCurrentRoom as InterviewRoomModel?) ?.isRoomOwner()!!) {
                ivShare.visibility = View.GONE
                ivColose.visibility = View.GONE
            }
        }
    }

    override fun observeLiveData() {
        mInterviewRoomVm.showLeaveInterviewLiveData.observe(this, Observer {
            ivColose.visibility=  if(it){
                View.VISIBLE
            }else{
                View.GONE
            }
        })
    }

    override fun initViewData() {

        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
        pubChatView.setAdapter(PubChatAdapter())
        lifecycle.addObserver(mWelComeReceiver)
        lifecycle.addObserver(mInputMsgReceiver)
        lifecycle.addObserver(pubChatView)
        ivMsg.setOnClickListener {
            RoomInputDialog()
                .apply {
                    sendPubCall = { msgEdit ->
                        mInputMsgReceiver.buildMsg(msgEdit)
                    }
                }
                .show(childFragmentManager, "RoomInputDialog")
        }
        ivColose.setOnClickListener {
            mInterviewRoomVm.endRoom()
        }
        ivShare.setOnClickListener {
            showShare()
        }
        ivSpeeker.setOnClickListener {
            if (!ivSpeeker.isSelected) {
                mInterviewRoom.muteAllRemoteAudio(true)
            } else {
                mInterviewRoom.muteAllRemoteAudio(false)
            }
            ivSpeeker.isSelected = !ivSpeeker.isSelected
        }

        flCloseAudio.setOnClickListener {
            if (!flCloseAudio.isSelected) {
                mInterviewRoom.muteLocalAudio(true)
            } else {
                mInterviewRoom.muteLocalAudio(false)
            }
            flCloseAudio.isSelected = !flCloseAudio.isSelected
        }
        flCloseVideo.setOnClickListener {
            if (!flCloseVideo.isSelected) {
                mInterviewRoom.muteLocalVideo(true)
            } else {
                mInterviewRoom.muteLocalVideo(false)
            }
            flCloseVideo.isSelected = !flCloseVideo.isSelected
        }
        flHangup.setOnClickListener {
            mInterviewRoomVm.levelInterviewRoom()
        }
    }

    private fun showShare() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND;
        sendIntent.putExtra(Intent.EXTRA_TEXT, (RoomManager.mCurrentRoom as InterviewRoomModel).interview.shareInfo.content);
        sendIntent.type = "text/plain";
        startActivity(sendIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        RoomManager.removeRoomLifecycleMonitor(mRoomLifecycleMonitor)
    }

    override fun getLayoutId(): Int {
        return R.layout.interview_fragment_room_cover
    }


}