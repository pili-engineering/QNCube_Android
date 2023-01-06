package com.niucube.module.videowatch

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hipi.vm.activityVm
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.module.videowatch.databinding.FragmentMovieChatBinding
import com.niucube.module.videowatch.databinding.ItemChatMemberBinding
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.been.BaseRoomEntity
import com.qiniudemo.baseapp.been.RoomMember
import com.qiniudemo.baseapp.been.findValueOfKey
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast

class ChatFragment : BaseFragment<FragmentMovieChatBinding>() {

    private val mMemberAdapter by lazy { MemberAdapter() }
    private val roomVm by activityVm<VideoRoomVm>()
    private val videoSourceVm by activityVm<VideoSourceVm>()

    private var roomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            if (roomEntity.isRoomHost()) {
                binding.llHostInvite.visibility = View.VISIBLE
                binding.tvFloatLinkInvite.visibility = View.VISIBLE
            } else {
                binding.llHostInvite.visibility = View.INVISIBLE
                binding.tvFloatLinkInvite.visibility = View.GONE
            }
        }
    }
    var goOnlineUserPageCall = {}
    var goManagerOnlineUserPageCall = {}
    var goInviteOnlineUserPageCall = {}
    var goMovieListPageCall = {}
    var goMicSeatPageCall = {}


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RoomManager.addRoomLifecycleMonitor(roomLifecycleMonitor)
        binding.llOnlineUser.setOnClickListener {
            goOnlineUserPageCall.invoke()
        }
        binding.ivToKick.setOnClickListener {
            goManagerOnlineUserPageCall.invoke()
        }
        binding.tvChangeMovie.setOnClickListener {
            goMovieListPageCall.invoke()
        }

        binding.flFloatLinkInvite.setOnClickListener {
            if (roomVm.mRtcRoom.mMicSeats.size > 1) {
                goMicSeatPageCall.invoke()
            } else {
                goInviteOnlineUserPageCall.invoke()
            }
        }
        binding.ivInviteCode.setOnClickListener {
            (RoomManager.mCurrentRoom as BaseRoomEntity?)
                ?.roomInfo?.params?.findValueOfKey("invitationCode")?.let {
                    val cm =
                        (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE)) as ClipboardManager
                    val mClipData = ClipData.newPlainText("Label", it);
                    cm.setPrimaryClip(mClipData);
                    "邀请码 ${it} 已经复制".asToast()
                }
        }

        binding.chatView.attachActivity(requireActivity())
        binding.chatView.attachListener()
        binding.chatView.setInputAutoChangeHeight(true)
        binding.tvMember.text = "成员 >"
        binding.rycMember.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rycMember.adapter = mMemberAdapter

        roomVm.roomInfoLiveData.observe(this.viewLifecycleOwner, Observer {
            " <font color='#69BCDB'> ${it.roomInfo?.totalUsers ?: "0"}</font> <font color='#BFBFBF'> 人在线</font>"
            binding.tvMember.text = "成员 ${it.roomInfo?.totalUsers ?: "0"}>"
            mMemberAdapter.setNewData(ArrayList<RoomMember>().apply {
                addAll(it.allUserList.filterIndexed { index, _ ->
                    index < 4
                })
            })
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ChatFragment()
    }

    class MemberAdapter : QSmartViewBindAdapter<RoomMember, ItemChatMemberBinding>() {

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemChatMemberBinding>,
            item: RoomMember
        ) {
            Glide.with(mContext)
                .load(item.avatar)
                .into(helper.binding.ivAvatar)
        }
    }
}