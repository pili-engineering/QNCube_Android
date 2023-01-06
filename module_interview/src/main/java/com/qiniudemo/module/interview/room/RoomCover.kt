package com.qiniudemo.module.interview.room

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.hipi.vm.activityVm
import com.qiniu.bzuicomp.bottominput.RoomInputDialog
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.module.interview.been.InterviewRoomModel
import com.qiniudemo.module.interview.databinding.InterviewFragmentRoomCoverBinding


/**
 * 面试覆盖层
 */
class RoomCover : BaseFragment<InterviewFragmentRoomCoverBinding>() {

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
    private val mInputMsgReceiver by lazy {
        mInterviewRoomVm.mInputMsgReceiver
    }
    private val mWelComeReceiver by lazy {
        mInterviewRoomVm.mWelComeReceiver
    }

    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            binding.clCoverContainer.visibility = View.VISIBLE
            binding.roomTittle.text = (roomEntity as InterviewRoomModel).interview.title
            mWelComeReceiver.sendEnterMsg()
        }

        override fun onRoomEntering(roomEntity: RoomEntity) {
            super.onRoomEntering(roomEntity)
            if ((RoomManager.mCurrentRoom as InterviewRoomModel?)?.isRoomOwner()!!) {
                binding.ivShare.visibility = View.GONE
                binding.ivColose.visibility = View.GONE
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mInterviewRoomVm.showLeaveInterviewLiveData.observe(this.viewLifecycleOwner, Observer {
            binding.ivColose.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
        binding.pubChatView.setAdapter(PubChatAdapter())
        lifecycle.addObserver(mWelComeReceiver)
        lifecycle.addObserver(mInputMsgReceiver)
        lifecycle.addObserver(binding.pubChatView)
        binding.ivMsg.setOnClickListener {
            RoomInputDialog()
                .apply {
                    sendPubCall = { msgEdit ->
                        mInputMsgReceiver.buildMsg(msgEdit)
                    }
                }
                .show(childFragmentManager, "RoomInputDialog")
        }
        binding.ivColose.setOnClickListener {
            mInterviewRoomVm.endRoom()
        }
        binding.ivShare.setOnClickListener {
            showShare()
        }
        binding.ivSpeeker.setOnClickListener {
            if (!binding.ivSpeeker.isSelected) {
                mInterviewRoom.muteAllRemoteAudio(true)
            } else {
                mInterviewRoom.muteAllRemoteAudio(false)
            }
            binding.ivSpeeker.isSelected = !binding.ivSpeeker.isSelected
        }

        binding.flCloseAudio.setOnClickListener {
            if (!binding.flCloseAudio.isSelected) {
                mInterviewRoom.muteLocalAudio(true)
            } else {
                mInterviewRoom.muteLocalAudio(false)
            }
            binding.flCloseAudio.isSelected = !binding.flCloseAudio.isSelected
        }
        binding.flCloseVideo.setOnClickListener {
            if (!binding.flCloseVideo.isSelected) {
                mInterviewRoom.muteLocalVideo(true)
            } else {
                mInterviewRoom.muteLocalVideo(false)
            }
            binding.flCloseVideo.isSelected = !binding.flCloseVideo.isSelected
        }
        binding.flHangup.setOnClickListener {
            mInterviewRoomVm.levelInterviewRoom()
        }
    }

    private fun showShare() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND;
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            (RoomManager.mCurrentRoom as InterviewRoomModel).interview.shareInfo.content
        );
        sendIntent.type = "text/plain";
        startActivity(sendIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        RoomManager.removeRoomLifecycleMonitor(mRoomLifecycleMonitor)
    }
}