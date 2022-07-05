package com.niucube.module.videowatch

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.base_mvvm.refresh.SmartRecyclerView
import com.hipi.vm.activityVm
import com.hipi.vm.backGround
import com.niucube.basemutableroom.absroom.RtcOperationCallback
import com.niucube.comproom.RoomManager
import com.niucube.rtm.RtmCallBack
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniudemo.baseapp.RecyclerFragment
import com.qiniudemo.baseapp.been.RoomMember
import com.qiniudemo.baseapp.ext.asToast
import kotlinx.android.synthetic.main.fragment_online_user.*
import kotlinx.android.synthetic.main.item_online_user.view.*

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
        smartRecyclerView
    }

    override val adapter: BaseQuickAdapter<RoomMemberWrap, *> by lazy { OnlineUserAdapter() }

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

    override val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            doWork {
                val info = roomVm.refreshRoomInfo()
                smartRecyclerView?.onFetchDataFinish(
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
                tvSize?.text = "好友列表 (${info.roomInfo?.totalUsers})"
            }
            catchError {
                smartRecyclerView?.onFetchDataError()
            }
        }
    }

    override fun initViewData() {
        super.initViewData()

        optionType = arguments?.getInt("optionType") ?: 3
        ivClose.setOnClickListener {
            backCall.invoke()
        }
        if (optionType != option_type_view) {
            llOptionInvite.visibility = View.VISIBLE
        } else {
            llOptionInvite.visibility = View.GONE
        }
        llSelectAll.setOnClickListener { cbSelectAll.performClick() }
        cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            adapter.data.forEach {
                it.selected = isChecked
            }
            adapter.notifyDataSetChanged()
        }

        tvOK.setOnClickListener {
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
                            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",-1,
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

    inner class OnlineUserAdapter : BaseQuickAdapter<RoomMemberWrap, BaseViewHolder>(
        R.layout.item_online_user,
        ArrayList<RoomMemberWrap>()
    ) {
        override fun convert(helper: BaseViewHolder, item: RoomMemberWrap) {
            helper.itemView.tvName.text = item.mRoomMember.name
            Glide.with(mContext)
                .load(item.mRoomMember.avatar)
                .into(helper.itemView.ivAvatar)
            if (optionType == option_type_view) {
                helper.itemView.setOnClickListener {}
            } else {
                helper.itemView.setOnClickListener {
                    helper.itemView.checkboxInvite.performClick()
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
            helper.itemView.checkboxInvite.isChecked = item.selected
            helper.itemView.checkboxInvite.setOnCheckedChangeListener { buttonView, isChecked ->
                item.selected = isChecked
            }
        }
    }
}