package com.niucube.module.amusement


import android.Manifest
import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.happy_dialog.FinalDialogFragment
import com.hapi.ut.ViewUtil
import com.hipi.vm.lazyVm
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.niucube.qnrtcsdk.RoundTextureView
import com.qiniu.jsonutil.JsonUtils
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.KeepLight
import com.qiniudemo.baseapp.been.UserExtProfile
import com.qiniudemo.baseapp.been.hostId
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_amusement_room.*
import kotlinx.android.synthetic.main.item_micseat.view.*


@Route(path = RouterConstant.Amusement.AmusementRoom)
class AmusementRoomActivity : BaseActivity() {

    private val roomVm by lazyVm<RoomViewModel>()

    @Autowired
    @JvmField
    var solutionType = ""

    @Autowired
    @JvmField
    var roomId = ""

    //进入房间订阅还是拉流模式
    @Autowired
    @JvmField
    var isUserJoinRTC = false

    private val mMicSeatsAdapter by lazy {
        MicSeatsAdapter()
    }

    private val mMicSeatsSurfaceAdapter by lazy { MicSeatsSurfaceAdapter() }

    private val micSeatListener = object : UserMicSeatListener {
        override fun onUserSitDown(micSeat: LazySitUserMicSeat) {
            mMicSeatsAdapter.getLastSeat(micSeat).let {
                it.seat = micSeat
                mMicSeatsAdapter.notifyItemChanged(mMicSeatsAdapter.data.indexOf(it))
            }
            if (micSeat.isMySeat()) {
                mMicSeatsAdapter.notifyDataSetChanged()
            }
            mMicSeatsSurfaceAdapter.addData(micSeat)
            recyMicSeatSurfaces.post {
                val index = mMicSeatsSurfaceAdapter.data.indexOf(micSeat)
                val container = (mMicSeatsSurfaceAdapter.getViewByPosition(
                    index,
                    R.id.qnSurfaceViewContainer
                ) as ViewGroup?) ?: return@post
                container.addView(
                    RoundTextureView(this@AmusementRoomActivity).apply {
                        roomVm.mRtcRoom.setUserCameraWindowView(micSeat.uid, this)
                        setRadius(ViewUtil.dip2px(6f).toFloat())
                    },
                    ViewGroup.LayoutParams(
                        container.width,
                        container.height
                    )
                )
            }
        }

        override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {
            mMicSeatsAdapter.getUserSeat(micSeat).let {
                mMicSeatsAdapter.remove(mMicSeatsAdapter.data.indexOf(it))
                mMicSeatsAdapter.addData(LazySitUserMicSeatWrap())
            }
            if (micSeat.isMySeat()) {
                mMicSeatsAdapter.notifyDataSetChanged()
            }

            val index = mMicSeatsSurfaceAdapter.data.indexOf(micSeat)
            val container = (mMicSeatsSurfaceAdapter.getViewByPosition(
                index,
                R.id.qnSurfaceViewContainer
            ) as ViewGroup?)
            container?.removeAllViews()
            mMicSeatsSurfaceAdapter.remove(mMicSeatsSurfaceAdapter.data.indexOf(micSeat))
            if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                "房主离开房间".asToast()
                roomVm.endRoom()
                finish()
            }
        }

        override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {
            mMicSeatsAdapter.getUserSeat(micSeat)?.let {
                mMicSeatsAdapter.notifyItemChanged(mMicSeatsAdapter.data.indexOf(it))
            }
        }

        override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {
            mMicSeatsAdapter.getUserSeat(micSeat).let {
                mMicSeatsAdapter.notifyItemChanged(mMicSeatsAdapter.data.indexOf(it))
            }
        }

        override fun onAudioForbiddenStatusChanged(seat: LazySitUserMicSeat, msg: String) {
            super.onAudioForbiddenStatusChanged(seat, msg)
            if (seat.isForbiddenAudioByManager) {
                "${seat.uid} 被管理员关闭麦克风".asToast()
            } else {
                "${seat.uid} 被管理员打开麦克风".asToast()
            }
            if (seat.isMySeat()) {
                roomVm.mRtcRoom.muteLocalAudio(seat.isForbiddenAudioByManager)
            }
            mMicSeatsAdapter.getUserSeat(seat).let {
                mMicSeatsAdapter.notifyItemChanged(mMicSeatsAdapter.data.indexOf(it))
            }
        }

        override fun onVideoForbiddenStatusChanged(seat: LazySitUserMicSeat, msg: String) {
            super.onVideoForbiddenStatusChanged(seat, msg)
            if (seat.isForbiddenVideoByManager) {
                "${seat.uid} 被管理员关闭摄像头".asToast()
            } else {
                "${seat.uid} 被管理员打开摄像头".asToast()
            }
            if (seat.isMySeat()) {
                roomVm.mRtcRoom.muteLocalVideo(seat.isForbiddenVideoByManager)
            }
            mMicSeatsAdapter.getUserSeat(seat).let {
                mMicSeatsAdapter.notifyItemChanged(mMicSeatsAdapter.data.indexOf(it))
            }
        }

        override fun onSyncMicSeats(seats: List<LazySitUserMicSeat>) {
            seats.forEachIndexed { index, lazySitUserMicSeat ->
                mMicSeatsAdapter.data[index].seat = lazySitUserMicSeat
                mMicSeatsAdapter.notifyItemChanged(index)
            }
            mMicSeatsSurfaceAdapter.setNewData(ArrayList<LazySitUserMicSeat>().apply { addAll(seats) })

            recyMicSeatSurfaces.post {
                mMicSeatsSurfaceAdapter.data.forEachIndexed { index, lazySitUserMicSeat ->
                    val container = (mMicSeatsSurfaceAdapter.getViewByPosition(
                        index,
                        R.id.qnSurfaceViewContainer
                    ) as ViewGroup?) ?: return@post
                    container.addView(
                        RoundTextureView(this@AmusementRoomActivity).apply {
                            roomVm.mRtcRoom.setUserCameraWindowView(lazySitUserMicSeat.uid, this)
                            setRadius(ViewUtil.dip2px(6f).toFloat())
                        },
                        ViewGroup.LayoutParams(
                            container.width,
                            container.height
                        )
                    )
                }
            }
        }

        override fun onKickOutFromMicSeat(seat: LazySitUserMicSeat, msg: String) {
            super.onKickOutFromMicSeat(seat, msg)
            "${seat.uid} 被管理员下麦".asToast()
            //onUserSitUp(seat, false)
            if (seat.isMySeat()) {
                roomVm.sitUp()
            }
        }
    }


    @SuppressLint("CheckResult")
    override fun initViewData() {
        lifecycle.addObserver(KeepLight(this))
        val trans = supportFragmentManager.beginTransaction()
        trans.replace(R.id.flRoomCover, AmusementRoomFragment())
        trans.commit()
        roomVm.mRtcRoom.addUserMicSeatListener(micSeatListener)
        roomVm.mRtcRoom.setAudiencePlayerView(plRoomPlayer)

        roomVm.isUserJoinRTC = isUserJoinRTC
        val requestCall = {
            RxPermissions(this)
                .request(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
                )
                .doFinally {
                    //加入房间
                    roomVm.joinRoom(solutionType, roomId)
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
        }
        micSeatContainer.post {

            initWH(micSeatContainer, micSeatContainer.width, micSeatContainer.height)
            Log.d(
                "MicSeatLayoutManager",
                "   ${clMicSeatContainer.width}  ${clMicSeatContainer.height}  ${clMicSeatContainer.measuredWidth}  ${clMicSeatContainer.measuredHeight}  "
            )
            recyMicSeats.layoutManager = MicSeatLayoutManager()//GridLayoutManager(this, 3)//
            mMicSeatsAdapter.bindToRecyclerView(recyMicSeats)
            recyMicSeatSurfaces.layoutManager = MicSeatLayoutManager()//GridLayoutManager(this, 3)//
            mMicSeatsSurfaceAdapter.bindToRecyclerView(recyMicSeatSurfaces)
            requestCall.invoke()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_amusement_room
    }


    inner class MicSeatsSurfaceAdapter : BaseQuickAdapter<LazySitUserMicSeat, BaseViewHolder>(
        R.layout.item_micseat_surface,
        ArrayList<LazySitUserMicSeat>()
    ) {
        override fun convert(helper: BaseViewHolder, item: LazySitUserMicSeat) {
        }
    }

    class LazySitUserMicSeatWrap() {
        var seat: LazySitUserMicSeat? = null
    }

    inner class MicSeatsAdapter : BaseQuickAdapter<LazySitUserMicSeatWrap, BaseViewHolder>(
        R.layout.item_micseat,
        ArrayList<LazySitUserMicSeatWrap>().apply {
            for (i in 0..6) {
                add(LazySitUserMicSeatWrap())
            }
        }
    ) {

        fun getLastSeat(micSeat: LazySitUserMicSeat): LazySitUserMicSeatWrap {
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

        override fun convert(helper: BaseViewHolder, item: LazySitUserMicSeatWrap) {

            helper.itemView.setOnClickListener {
                if (TextUtils.isEmpty(item.seat?.uid)) {
                    if (roomVm.mRtcRoom.mClientRole != ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                        roomVm.sitDown()
                    }
                } else {
                    MicSeatInfoDialog.newInstance(item.seat?.uid ?: "")
                        .show(supportFragmentManager, "")
                }
            }

            if (roomVm.mRtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                helper.itemView.ivJia.visibility = View.GONE
            } else {
                helper.itemView.ivJia.visibility = View.VISIBLE
            }

            if (TextUtils.isEmpty(item.seat?.uid)) {
                helper.itemView.llFooter.visibility = View.VISIBLE
                helper.itemView.flContent.visibility = View.GONE
            } else {
                helper.itemView.llFooter.visibility = View.GONE
                helper.itemView.flContent.visibility = View.VISIBLE
                item.seat?.userExtension?.userExtProfile?.let {
                    val userExtProfile = JsonUtils.parseObject(it, UserExtProfile::class.java)
                    userExtProfile?.let {
                        Glide.with(mContext)
                            .load(it.avatar)
                            .apply(RequestOptions.bitmapTransform(RoundedCorners(ViewUtil.dip2px(6f))))
                            .into(helper.itemView.ivMicSeatAvatar)
                        helper.itemView.tvMicNick.text = it.name
                    }
                }

                if (roomVm.mRtcRoom.mClientRole == ClientRoleType.CLIENT_ROLE_PULLER) {
                    helper.itemView.ivMicSeatAvatar.isVisible = false
                } else {
                    helper.itemView.ivMicSeatAvatar.isVisible = !item.seat!!.isOpenVideo()
                }
                helper.itemView.ivMicrophoneStatus.isSelected = item.seat!!.isOpenAudio()
            }
        }

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
}