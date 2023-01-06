package com.niucube.audioroom

import android.Manifest
import android.annotation.SuppressLint
import android.text.Html
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hapi.baseframe.adapter.QRecyclerViewBindAdapter
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.hapi.ut.ViewUtil
import com.hipi.vm.lazyVm
import com.niucube.audioroom.databinding.ActivityAudioRoomBinding
import com.niucube.audioroom.databinding.ItemAudioSeatBinding
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.bzuicomp.bottominput.RoomInputDialog
import com.qiniu.bzuicomp.gift.*
import com.qiniu.bzuicomp.pubchat.PubChatAdapter
import com.qiniu.jsonutil.JsonUtils
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.KeepLight
import com.qiniudemo.baseapp.been.UserExtProfile
import com.qiniudemo.baseapp.been.asBaseRoomEntity
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.tbruyelle.rxpermissions2.RxPermissions

@Route(path = RouterConstant.VoiceChatRoom.voiceChatRoom)
class AudioRoomActivity : BaseActivity<ActivityAudioRoomBinding>() {

    @Autowired
    @JvmField
    var solutionType = ""

    @Autowired
    @JvmField
    var roomId = ""

    companion object {
        var isActivityDestory = true
    }

    private val roomVm by lazyVm<RoomViewModel>()

    private val micSeatAdapter by lazy {
        MicSeatsAdapter()
    }

    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
        }

        @SuppressLint("SetTextI18n")
        override fun onRoomEntering(roomEntity: RoomEntity) {
            super.onRoomEntering(roomEntity)
            binding.tvRoomName.text = Html.fromHtml(
                " <font color='#4A4A4A'>房间名 </font> <font color='#ffffff'> ${roomEntity.asBaseRoomEntity().roomInfo?.title ?: ""}</font>"
            )
            //   tvRoomName.setText(roomEntity.asBaseRoomEntity().roomInfo?.title ?: "")
        }
    }

    private val micSeatListener = object : UserMicSeatListener {

        @SuppressLint("NotifyDataSetChanged")
        override fun onUserSitDown(micSeat: LazySitUserMicSeat) {
            micSeatAdapter.getLastSeat().let {
                it.seat = micSeat
                micSeatAdapter.notifyItemChanged(micSeatAdapter.data.indexOf(it))
            }
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                micSeatAdapter.notifyDataSetChanged()
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {
            micSeatAdapter.getUserSeat(micSeat)?.let {
                micSeatAdapter.remove(micSeatAdapter.data.indexOf(it))
                micSeatAdapter.addData(LazySitUserMicSeatWrap())
            }
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                micSeatAdapter.notifyDataSetChanged()
            }
            if (micSeat.uid == RoomManager.mCurrentRoom?.asBaseRoomEntity()?.roomInfo?.creator) {
                "房主下线".asToast()
                binding.tvLeaveRoom.performClick()
            }
        }

        override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {}

        override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                binding.ivMicStatus.isSelected = !micSeat.isOpenAudio()
            }
            micSeatAdapter.getUserSeat(micSeat).let {
                micSeatAdapter.notifyItemChanged(micSeatAdapter.data.indexOf(it))
            }
        }

        override fun onAudioForbiddenStatusChanged(seat: LazySitUserMicSeat, msg: String) {
            if (seat.isForbiddenAudioByManager) {
                "${seat.uid} 被管理员关闭麦克风".asToast()
            } else {
                "${seat.uid} 被管理员打开麦克风".asToast()
            }
            if (seat.isMySeat(UserInfoManager.getUserId())) {
                roomVm.mRtcRoom.muteLocalAudio(seat.isForbiddenAudioByManager)
            }
            micSeatAdapter.getUserSeat(seat).let {
                micSeatAdapter.notifyItemChanged(micSeatAdapter.data.indexOf(it))
            }
        }

        override fun onKickOutFromMicSeat(seat: LazySitUserMicSeat, msg: String) {
            super.onKickOutFromMicSeat(seat, msg)
            "${seat.uid} 被管理员下麦".asToast()
            if (seat.isMySeat(UserInfoManager.getUserId())) {
                roomVm.sitUp()
            }
        }

        override fun onSyncMicSeats(seats: List<LazySitUserMicSeat>) {
            seats.forEachIndexed { index, lazySitUserMicSeat ->
                micSeatAdapter.data[index].seat = lazySitUserMicSeat
                micSeatAdapter.notifyItemChanged(index)
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun init() {
        roomVm.mTotalUsersLivedata.observe(this, Observer {
            binding.tvRoomMemberCount.text = it.size.toString()
        })
        lifecycle.addObserver(KeepLight(this))
        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
        binding.mRTCLogView.attachRTCClient(roomVm.mRtcRoom)
        isActivityDestory = false
        // gameFragment.addGameFragment(R.id.giftContainer, this)
        binding.recyMicSeats.layoutManager = GridLayoutManager(this, 3)//
        micSeatAdapter.bindToRecyclerView(binding.recyMicSeats)
        lifecycle.addObserver(binding.pubChatView)
        binding.pubChatView.setAdapter(PubChatAdapter())
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

        RxPermissions(this)
            .request(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
            .doFinally {
                //加入房间
                roomVm.joinRoom(solutionType, roomId)
                roomVm.mRtcRoom.addUserMicSeatListener(micSeatListener)
            }
            .subscribe {
                if (!it) {
                    CommonTipDialog.TipBuild()
                        .setContent("请开启必要权限")
                        .setListener(object : FinalDialogFragment.BaseDialogListener() {
                            override fun onDismiss(dialog: DialogFragment) {
                                super.onDismiss(dialog)
                                finish()
                            }
                        })
                        .build()
                        .show(supportFragmentManager, "CommonTipDialog")
                }
            }

        binding.tvLeaveRoom.setOnClickListener {
            if (roomVm.mRtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER
                && RoomManager.mCurrentRoom?.isRoomHost() == false
            ) {
                roomVm.sitUp()
                return@setOnClickListener
            }
            roomVm.endRoom()
            finish()
        }

        binding.ivMicStatus.setOnClickListener {
            val toOpen = !binding.ivMicStatus.isSelected
            val mySeat = roomVm.mRtcRoom.getUserSeat(UserInfoManager.getUserId())
            if (mySeat?.isForbiddenAudioByManager == true) {
                "管理关了你的麦".asToast()
                return@setOnClickListener
            }
            roomVm.mRtcRoom.muteLocalAudio(toOpen)
        }

        binding.tvShowInput.setOnClickListener {
            RoomInputDialog(2).apply {
                sendPubCall = {
                    roomVm.mInputMsgReceiver.buildMsg(it)
                }
            }
                .show(supportFragmentManager, "")
        }

        binding.ivDanmu.setOnClickListener {
            RoomInputDialog(2).apply {
                sendPubCall = {
                    roomVm.mDanmuTrackManager.buidMsg(it)
                }
            }
                .show(supportFragmentManager, "")
        }
        binding.ivGift.setOnClickListener {
            GiftPanDialog().show(supportFragmentManager, "")
        }
    }

    override fun onDestroy() {
        isActivityDestory = true
        roomVm.mRtcRoom.removeUserMicSeatListener(micSeatListener)
        RoomManager.removeRoomLifecycleMonitor(mRoomLifecycleMonitor)
        super.onDestroy()
    }


    //安卓重写返回键事件
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK
            && RoomManager.mCurrentRoom?.isJoined == true
        ) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    class LazySitUserMicSeatWrap() {
        var seat: LazySitUserMicSeat? = null
    }

    inner class MicSeatsAdapter : QRecyclerViewBindAdapter<LazySitUserMicSeatWrap, ItemAudioSeatBinding>() {
       init {
           data.addAll(ArrayList<LazySitUserMicSeatWrap>().apply {
               for (i in 0..6) {
                   add(LazySitUserMicSeatWrap())
               }
           })
       }
        fun getLastSeat(): LazySitUserMicSeatWrap {
            data.forEach {
                if (it.seat == null) {
                    return it
                }
            }
            return LazySitUserMicSeatWrap()
        }

        fun getUserSeat(seat: LazySitUserMicSeat): LazySitUserMicSeatWrap? {
            data.forEach {
                if (it.seat?.uid == seat.uid) {
                    return it
                }
            }
            return null
        }

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemAudioSeatBinding>,
            item: LazySitUserMicSeatWrap
        ) {
            val lp = helper.binding.qnSurfaceViewContainer.layoutParams as FrameLayout.LayoutParams
            var itemRoundedCorners = 0f;
            if (data.indexOf(item) == 0) {
                helper.binding.tvIndexId.visibility = View.GONE
                helper.binding.ivOpPopup.visibility = View.GONE
                itemRoundedCorners = 20f;
                lp.width = ViewUtil.dip2px(130f)
                lp.height = ViewUtil.dip2px(130f)
                lp.marginStart = ViewUtil.dip2px(20f)
                helper.binding.ivBgView.visibility = View.GONE
            } else {
                itemRoundedCorners = 1f;
                helper.binding.tvIndexId.visibility = View.VISIBLE
                helper.binding.ivOpPopup.visibility = View.VISIBLE
                lp.marginStart = ViewUtil.dip2px(0f)
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                helper.binding.ivBgView.visibility = View.VISIBLE
            }

            helper.binding.tvIndexId.text = data.indexOf(item).toString()
            helper.binding.qnSurfaceViewContainer.layoutParams = lp
            val lp2 = helper.itemView.layoutParams as GridLayoutManager.LayoutParams
            if (data.indexOf(item) == 0) {
                lp2.bottomMargin = ViewUtil.dip2px(10f)
            } else {
                lp2.bottomMargin = ViewUtil.dip2px(0f)
            }
            helper.itemView.layoutParams = lp2

            item.seat?.userExtension?.userExtProfile?.let {
                val userExtProfile = JsonUtils.parseObject(it, UserExtProfile::class.java)
                userExtProfile?.let {
                    Glide.with(mContext)
                        .load(it.avatar)
                        .apply(
                            RequestOptions.bitmapTransform(
                                RoundedCorners(
                                    ViewUtil.dip2px(
                                        itemRoundedCorners
                                    )
                                )
                            )
                        )
                        .into(helper.binding.ivMicSeatAvatar)
                    helper.binding.tvMicNick.text = it.name
                }
            }
            helper.binding.ivMicrophoneStatus.isSelected = item.seat?.isOpenAudio() ?: false

            if (item.seat != null) {
                helper.binding.flItemContent.visibility = View.VISIBLE
                helper.binding.flItemEmpty.visibility = View.GONE
            } else {
                helper.binding.flItemContent.visibility = View.GONE
                helper.binding.flItemEmpty.visibility = View.VISIBLE
                if (roomVm.mRtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                    // helper.itemView.ivJia.visibility = View.GONE
                    helper.binding.tvOperationText.text = "等待连线"
                } else {
                    // helper.itemView.ivJia.visibility = View.VISIBLE
                    helper.binding.tvOperationText.text = "点击上麦"
                }
            }

            helper.itemView.setOnClickListener {
                if (roomVm.mRtcRoom.mClientRole != ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                    //申请上麦
                    roomVm.applySitDown()
                }
            }
            val isHost = RoomManager.mCurrentRoom?.isRoomHost() ?: false
            if (isHost && data.indexOf(item) > 0) {
                helper.binding.ivOpPopup.visibility = View.VISIBLE
            } else {
                helper.binding.ivOpPopup.visibility = View.GONE
            }
            helper.binding.ivOpPopup.setOnClickListener {
                MicSeatInfoDialog.newInstance(item.seat?.uid ?: "").show(supportFragmentManager, "")
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            val manager = recyclerView.layoutManager
            if (manager is GridLayoutManager) {
                manager.spanSizeLookup = MySpanSizeLookup(this)
            }
        }
    }

    class MySpanSizeLookup(private val adapter: QRecyclerViewBindAdapter<*, *>) :
        GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            val type = adapter.getItemViewType(position)
            return if (position == 0 || type == BaseQuickAdapter.FOOTER_VIEW) {
                3
            } else {
                1
            }
        }
    }

}