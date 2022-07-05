package com.niucube.module.videowatch

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hipi.vm.activityVm
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.been.BaseRoomEntity
import com.qiniudemo.baseapp.been.RoomMember
import com.qiniudemo.baseapp.been.findValueOfKey
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast
import kotlinx.android.synthetic.main.fragment_movie_chat.*
import kotlinx.android.synthetic.main.item_chat_member.view.*

class ChatFragment : BaseFragment() {

    private val mMemberAdapter by lazy { MemberAdapter() }
    private val roomVm by activityVm<VideoRoomVm>()
    private val videoSourceVm by activityVm<VideoSourceVm>()

    private var roomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            if (roomEntity.isRoomHost()) {
                llHostInvite?.visibility = View.VISIBLE
                tvFloatLinkInvite?.visibility = View.VISIBLE
            } else {
                llHostInvite?.visibility = View.INVISIBLE
                tvFloatLinkInvite?.visibility = View.GONE
            }
        }
    }
    var goOnlineUserPageCall = {}
    var goManagerOnlineUserPageCall = {}
    var goInviteOnlineUserPageCall = {}
    var goMovieListPageCall = {}
    var goMicSeatPageCall = {}


    @SuppressLint("SetTextI18n")
    override fun initViewData() {
        RoomManager.addRoomLifecycleMonitor(roomLifecycleMonitor)
        llOnlineUser.setOnClickListener {
            goOnlineUserPageCall.invoke()
        }
        ivToKick.setOnClickListener {
            goManagerOnlineUserPageCall.invoke()
        }
        tvChangeMovie.setOnClickListener {
            goMovieListPageCall.invoke()
        }

        flFloatLinkInvite.setOnClickListener {
            if (roomVm.mRtcRoom.mMicSeats.size > 1) {
                goMicSeatPageCall.invoke()
            } else {
                goInviteOnlineUserPageCall.invoke()
            }
        }
        ivInviteCode.setOnClickListener {
            (RoomManager.mCurrentRoom as BaseRoomEntity?)
                ?.roomInfo?.params?.findValueOfKey("invitationCode")?.let {
                    val cm =
                        (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE)) as ClipboardManager
                    val mClipData = ClipData.newPlainText("Label", it);
                    cm.setPrimaryClip(mClipData);
                    "邀请码 ${it} 已经复制".asToast()
                }
        }

        chatView.attachActivity(requireActivity())
        chatView.attachListener()
        chatView.setInputAutoChangeHeight(true)
        tvMember.text = "成员 >"
        rycMember.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rycMember.adapter = mMemberAdapter

        roomVm.roomInfoLiveData.observe(this, Observer {
            " <font color='#69BCDB'> ${it.roomInfo?.totalUsers ?: "0"}</font> <font color='#BFBFBF'> 人在线</font>"
            tvMember.text = "成员 ${it.roomInfo?.totalUsers ?: "0"}>"
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

    override fun getLayoutId(): Int {
        return R.layout.fragment_movie_chat
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ChatFragment()
    }

    class MemberAdapter : BaseQuickAdapter<RoomMember, BaseViewHolder>(
        R.layout.item_chat_member,
        ArrayList<RoomMember>()
    ) {
        override fun convert(helper: BaseViewHolder, item: RoomMember) {
            Glide.with(mContext)
                .load(item.avatar)
                .into(helper.itemView.ivAvatar)
        }
    }
}