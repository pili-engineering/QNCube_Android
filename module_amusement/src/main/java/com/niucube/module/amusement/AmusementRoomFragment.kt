package com.niucube.module.amusement

import android.view.View
import com.hipi.vm.activityVm
import com.hipi.vm.bgDefault
import com.hipi.vm.lazyVm
import com.niucube.absroom.IAudienceJoinListener
import com.niucube.absroom.seat.UserExtension
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
//import com.niucube.compui.game.GameFragment
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.bzuicomp.bottominput.RoomInputDialog
import com.qiniu.bzuicomp.pubchat.InputMsgReceiver
import com.qiniu.bzuicomp.pubchat.PubChatAdapter
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.been.asBaseRoomEntity
import com.qiniudemo.baseapp.ext.asToast
import kotlinx.android.synthetic.main.fragment_room_cover.*

class AmusementRoomFragment : BaseFragment() {

    private val roomVm by activityVm<RoomViewModel>()
    private val mInputMsgReceiver = InputMsgReceiver()
//    private val gameFragment by lazy {
//        GameFragment()
//    }

    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
        }

        override fun onRoomEntering(roomEntity: RoomEntity) {
            super.onRoomEntering(roomEntity)
            tvRoomName.setText(roomEntity.asBaseRoomEntity().roomInfo?.title ?: "")
        }
    }

    //麦位监听
    private val mMicSeatListener = object : UserMicSeatListener {

        override fun onUserSitDown(micSeat: LazySitUserMicSeat) {

            if (micSeat.isMySeat()) {
                //自己上麦
                ivCameraStatus.visibility = View.VISIBLE
                ivMicrophoneStatus.visibility = View.VISIBLE
            }
        }

        override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {
            if (micSeat.isMySeat()) {
                ivCameraStatus?.visibility = View.GONE
                ivMicrophoneStatus?.visibility = View.GONE
            }
        }

        override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {
            if (micSeat.isMySeat()) {
                ivCameraStatus.isSelected = !micSeat.isOpenVideo()
            }
        }

        override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {
            if (micSeat.isMySeat()) {
                ivMicrophoneStatus.isSelected = !micSeat.isOpenAudio()
            }
        }
    }

    override fun initViewData() {
       // gameFragment.addGameFragment(R.id.flGameContainer, requireActivity())
        roomVm.mTotalUsersLivedata.observe(this) {
            tvRoomMemb.text = it.toString()
        }
        lifecycle.addObserver(pubChatView)
        pubChatView.setAdapter(PubChatAdapter())
        lifecycle.addObserver(mInputMsgReceiver)
        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
        roomVm.mRtcRoom.addUserMicSeatListener(mMicSeatListener)

        tvRoomMemb.setOnClickListener {

        }
        tvNotice.setOnClickListener {

        }
        ivMicrophoneStatus.setOnClickListener {
            val toOpen = ivMicrophoneStatus.isSelected
            val mySeat = roomVm.mRtcRoom.getUserSeat(UserInfoManager.getUserId())
            if (mySeat?.isForbiddenAudioByManager == true) {
                "管理关了你的麦".asToast()
                return@setOnClickListener
            }
            roomVm.mRtcRoom.muteLocalAudio(!toOpen)
        }

        ivCameraStatus.setOnClickListener {
            val toOpen = ivCameraStatus.isSelected
            val mySeat = roomVm.mRtcRoom.getUserSeat(UserInfoManager.getUserId())
            if (mySeat?.isForbiddenVideoByManager == true) {
                "管理关了你的麦".asToast()
                return@setOnClickListener
            }
            roomVm.mRtcRoom.muteLocalVideo(!toOpen)
        }

        ivMenu.setOnClickListener {
            roomVm.sitUp()
        }

        ivClose.setOnClickListener {
            roomVm.endRoom()
            requireActivity().finish()
        }

        tvShowInput.setOnClickListener {
            RoomInputDialog().apply {
                sendPubCall = {
                    mInputMsgReceiver.buildMsg(it)
                }
            }
                .show(childFragmentManager, "")
        }

//        ivGame.setOnClickListener {
//            gameFragment.startOrHide()
//        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_room_cover
    }
}