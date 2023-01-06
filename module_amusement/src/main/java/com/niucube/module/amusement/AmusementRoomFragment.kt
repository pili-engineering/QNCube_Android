package com.niucube.module.amusement

import android.os.Bundle
import android.view.View
import com.hipi.vm.activityVm
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.niucube.module.amusement.databinding.FragmentRoomCoverBinding
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.bzuicomp.bottominput.RoomInputDialog
import com.qiniu.bzuicomp.gift.GiftPanDialog
import com.qiniu.bzuicomp.pubchat.PubChatAdapter
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.been.asBaseRoomEntity
import com.qiniudemo.baseapp.ext.asToast

class AmusementRoomFragment : BaseFragment<FragmentRoomCoverBinding>() {

    private val roomVm by activityVm<RoomViewModel>()

    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
        }

        override fun onRoomEntering(roomEntity: RoomEntity) {
            super.onRoomEntering(roomEntity)
            binding.tvRoomName.setText(roomEntity.asBaseRoomEntity().roomInfo?.title ?: "")
        }
    }

    //麦位监听
    private val mMicSeatListener = object : UserMicSeatListener {

        override fun onUserSitDown(micSeat: LazySitUserMicSeat) {
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                //自己上麦
                binding.ivCameraStatus.visibility = View.VISIBLE
                binding.ivMicrophoneStatus.visibility = View.VISIBLE
                binding.ivMenu.visibility = View.VISIBLE
            }
        }

        override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                binding.ivCameraStatus.visibility = View.GONE
                binding.ivMicrophoneStatus.visibility = View.GONE
                binding.ivMenu.visibility = View.GONE
            }
        }

        override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                binding.ivCameraStatus.isSelected = !micSeat.isOpenVideo()
            }
        }

        override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                binding.ivMicrophoneStatus.isSelected = !micSeat.isOpenAudio()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // gameFragment.addGameFragment(R.id.flGameContainer, requireActivity())
        roomVm.mTotalUsersLivedata.observe(this.viewLifecycleOwner) {
            binding.tvRoomMemb.text = it.toString()
        }
        lifecycle.addObserver(binding.pubChatView)
        binding.pubChatView.setAdapter(PubChatAdapter())

        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
        roomVm.mRtcRoom.addUserMicSeatListener(mMicSeatListener)
        binding.mRTCLogView.attachRTCClient(roomVm.mRtcRoom)

        lifecycle.addObserver(roomVm.mInputMsgReceiver)
        lifecycle.addObserver(roomVm.mGiftTrackManager)
        lifecycle.addObserver(roomVm.mBigGiftManager)
        lifecycle.addObserver(roomVm.mWelComeReceiver)
        lifecycle.addObserver(roomVm.mDanmuTrackManager)
        roomVm.mBigGiftManager.attch(binding.mBigGiftView)
        roomVm.mGiftTrackManager.addTrackView(binding.giftShow1)
        roomVm.mGiftTrackManager.addTrackView(binding.giftShow2)
        roomVm.mGiftTrackManager.addTrackView(binding.giftShow3)
        roomVm.mDanmuTrackManager.addTrackView(binding.danmu1)
        roomVm.mDanmuTrackManager.addTrackView(binding.danmu2)
        var lastGiftId = ""
        roomVm.mGiftTrackManager.extGiftMsgCall = {
            if (it.sendGift.giftId != lastGiftId) {
                lastGiftId = it.sendGift.giftId
                roomVm.mBigGiftManager.playInQueen(it)
            }
        }

        binding.ivDanmu.setOnClickListener {
            RoomInputDialog().apply {
                sendPubCall = {
                    roomVm.mDanmuTrackManager.buidMsg(it)
                }
            }
                .show(childFragmentManager, "")
        }
        binding.ivGift.setOnClickListener {
            GiftPanDialog().show(childFragmentManager, "")
        }

        binding.tvRoomMemb.setOnClickListener {

        }
        binding.tvNotice.setOnClickListener {

        }
        binding.ivMicrophoneStatus.setOnClickListener {
            val toOpen = binding.ivMicrophoneStatus.isSelected
            val mySeat = roomVm.mRtcRoom.getUserSeat(UserInfoManager.getUserId())
            if (mySeat?.isForbiddenAudioByManager == true) {
                "管理关了你的麦".asToast()
                return@setOnClickListener
            }
            roomVm.mRtcRoom.muteLocalAudio(!toOpen)
        }

        binding.ivCameraStatus.setOnClickListener {
            val toOpen = binding.ivCameraStatus.isSelected
            val mySeat = roomVm.mRtcRoom.getUserSeat(UserInfoManager.getUserId())
            if (mySeat?.isForbiddenVideoByManager == true) {
                "管理关了你的麦".asToast()
                return@setOnClickListener
            }
            roomVm.mRtcRoom.muteLocalVideo(!toOpen)
        }

        binding.ivMenu.setOnClickListener {
            roomVm.sitUp()
        }

        binding.ivClose.setOnClickListener {
            roomVm.endRoom()
            requireActivity().finish()
        }

        binding.tvShowInput.setOnClickListener {
            RoomInputDialog().apply {
                sendPubCall = {
                    roomVm.mInputMsgReceiver.buildMsg(it)
                }
            }
                .show(childFragmentManager, "")
        }
//        ivGame.setOnClickListener {
//            gameFragment.startOrHide()
//        }
    }

}