package com.niucube.module.videowatch

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hapi.baseframe.smartrecycler.SmartRecyclerView
import com.hipi.vm.activityVm
import com.hipi.vm.backGround
import com.niucube.absroom.RtcOperationCallback
import com.niucube.comproom.RoomManager
import com.niucube.module.videowatch.databinding.ItemOnlineUserBinding
import com.niucube.rtm.RtmCallBack
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniudemo.baseapp.RecyclerFragment
import com.qiniudemo.baseapp.been.RoomMember
import com.qiniudemo.baseapp.ext.asToast

/**
 * 在线用户列表
 */
class OnlineUserFragment : RecyclerFragment<OnlineUserFragment.RoomMemberWrap>() {

    companion object {
        //管理用户
        val option_type_manager = 1

        //邀请用户
        val option_type_invite = 2

        //
        val option_type_view = 3

        @JvmStatic
        fun newInstance(type: Int) =
            OnlineUserFragment().apply {
                val bundle = Bundle()
                bundle.putInt("optionType", type)
                arguments = bundle
            }
    }

    private var optionType = option_type_view
    private val roomVm by activityVm<VideoRoomVm>()
    private val videoSourceVm by activityVm<VideoSourceVm>()
    var backCall = {}

    override val mSmartRecycler: SmartRecyclerView by lazy {
        view?.findViewById(R.id.smartRecyclerView)!!
    }

    override val adapter by lazy { OnlineUserAdapter() }

    override val layoutManager: RecyclerView.LayoutManager by lazy {
        LinearLayoutManager(
            requireContext()
        )
    }

    override fun loadMoreNeed(): Boolean {
        return false
    }

    private fun isOnMic(uid: String): Boolean {
        roomVm.mRtcRoom.mMicSeats.forEach {
            if (it.uid == uid) {
                return true
            }
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    override val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            doWork {
                val info = roomVm.refreshRoomInfo()
                mSmartRecycler.onFetchDataFinish(
                    info.allUserList
                        .filter {
                            var isfilter = true
                            if (optionType == option_type_invite || optionType == option_type_manager) {
                                if (UserInfoManager.getUserId() == it.userId) {
                                    isfilter = false
                                }
                            }
                            if (optionType == option_type_invite && isfilter) {
                                isfilter = !isOnMic(it.userId)
                            }
                            isfilter
                        }
                        .map {
                            RoomMemberWrap(it)
                        }, true, true
                )
                view?.findViewById<TextView>(R.id.tvSize)?.text =
                    "好友列表 (${info.roomInfo?.totalUsers})"
            }
            catchError {
                mSmartRecycler.onFetchDataError()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        optionType = arguments?.getInt("optionType") ?: 3
        view.findViewById<View>(R.id.ivClose).setOnClickListener {
            backCall.invoke()
        }
        if (optionType != option_type_view) {
            view.findViewById<View>(R.id.llOptionInvite).visibility = View.VISIBLE
        } else {
            view.findViewById<View>(R.id.llOptionInvite).visibility = View.GONE
        }
        view.findViewById<View>(R.id.llSelectAll)
            .setOnClickListener { view.findViewById<View>(R.id.cbSelectAll).performClick() }
        view.findViewById<CheckBox>(R.id.cbSelectAll)
            .setOnCheckedChangeListener { buttonView, isChecked ->
                adapter.data.forEach {
                    it.selected = isChecked
                }
                adapter.notifyDataSetChanged()
            }

        view.findViewById<View>(R.id.tvOK).setOnClickListener {
            var sendedUserCount = 0
            adapter.data.forEach {
                if (it.selected) {
                    sendedUserCount++
                    if (option_type_invite == optionType) {
                        if (it.mRoomMember.userId == UserInfoManager.getUserId()) {
                            return@forEach
                        }
                        roomVm.mInvitationProcessor.invite(
                            "用户 ${UserInfoManager.getUserInfo()?.nickname} 邀请你一起连麦，是否加入？",
                            it.mRoomMember.userId,
                            RoomManager.mCurrentRoom?.provideImGroupId() ?: "", -1,
                            object : RtmCallBack {
                                override fun onSuccess() {
                                    sendedUserCount--
                                    showLoading(false)
                                }

                                override fun onFailure(code: Int, msg: String) {
                                    "邀请用户${it.mRoomMember.nickname}失败".asToast()
                                    sendedUserCount--
                                    if (sendedUserCount == 0) {
                                        showLoading(false)
                                    }
                                }
                            })
                    }
                    if (option_type_manager == optionType) {
                        if (it.mRoomMember.userId == UserInfoManager.getUserId()) {
                            return@forEach
                        }
                        roomVm.mRtcRoom.kickOutFromRoom(it.mRoomMember.userId, "房主请你离开房间",
                            object : RtcOperationCallback {
                                override fun onSuccess() {}

                                override fun onFailure(errorCode: Int, msg: String) {
                                    "移除用户${it.mRoomMember.nickname}失败".asToast()
                                }
                            })
                    }
                }
            }
            backCall.invoke()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_online_user
    }

    class RoomMemberWrap(var mRoomMember: RoomMember, var selected: Boolean = false)

    inner class OnlineUserAdapter : QSmartViewBindAdapter<RoomMemberWrap, ItemOnlineUserBinding>() {
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemOnlineUserBinding>,
            item: RoomMemberWrap
        ) {
            helper.binding.tvName.text = item.mRoomMember.name
            Glide.with(mContext)
                .load(item.mRoomMember.avatar)
                .into(helper.binding.ivAvatar)
            if (optionType == option_type_view) {
                helper.itemView.setOnClickListener {}
            } else {
                helper.itemView.setOnClickListener {
                    helper.binding.checkboxInvite.performClick()
                }
//                var isUserOnSeat = false
//                roomVm.mRtcRoom.mMicSeats.forEach {
//                    if (it.uid == item.mRoomMember.userId) {
//                        isUserOnSeat = true
//                        return@forEach
//                    }
//                }
//                if (optionType == option_type_invite && (isUserOnSeat || UserInfoManager.getUserId() == item.mRoomMember.userId)) {
//                    item.selected = false
//                    helper.itemView.checkboxInvite.visibility = View.GONE
//                }
//                if (optionType == option_type_manager && (UserInfoManager.getUserId() == item.mRoomMember.userId)) {
//                    item.selected = false
//                    helper.itemView.checkboxInvite.visibility = View.GONE
//                }
            }
            helper.binding.checkboxInvite.isChecked = item.selected
            helper.binding.checkboxInvite.setOnCheckedChangeListener { _, isChecked ->
                item.selected = isChecked
            }
        }
    }
}