package com.niucube.module.amusement


import android.Manifest
import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.niucube.absroom.IAudienceJoinListener
import com.niucube.absroom.seat.UserExtension
import com.niucube.comproom.ClientRoleType
import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.niucube.qnrtcsdk.RoundTextureView
import com.qiniu.droid.rtc.QNTextureView
import com.qiniu.jsonutil.JsonUtils
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.been.BaseRoomEntity
import com.qiniudemo.baseapp.been.UserExtProfile
import com.qiniudemo.baseapp.been.hostId
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_amusement_room.*
import kotlinx.android.synthetic.main.item_micseat.view.*
import kotlinx.android.synthetic.main.item_micseat_surface.view.*
import java.io.InputStream


@Route(path = RouterConstant.Amusement.AmusementRoom)
class AmusementRoomActivity : BaseActivity() {

    private val roomVm by lazyVm<RoomViewModel>()

    @Autowired
    @JvmField
    var solutionType = ""

    @Autowired
    @JvmField
    var roomId = ""

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
                container.addView(RoundTextureView(this@AmusementRoomActivity).apply {
                    roomVm.mRtcRoom.setUserCameraWindowView(micSeat.uid, this)
                    setRadius(ViewUtil.dip2px(6f).toFloat())
                })
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
                    container.addView(RoundTextureView(this@AmusementRoomActivity).apply {
                        roomVm.mRtcRoom.setUserCameraWindowView(lazySitUserMicSeat.uid, this)
                        setRadius(ViewUtil.dip2px(6f).toFloat())
                    })
                }
            }
        }

        override fun onKickOutFromMicSeat(seat: LazySitUserMicSeat, msg: String) {
            super.onKickOutFromMicSeat(seat, msg)
            "${seat.uid} 被管理员下麦".asToast()
            onUserSitUp(seat, false)
        }
    }


    @SuppressLint("CheckResult")
    override fun initViewData() {

        val trans = supportFragmentManager.beginTransaction()
        trans.replace(R.id.flRoomCover, AmusementRoomFragment())
        trans.commit()
        roomVm.mRtcRoom.addUserMicSeatListener(micSeatListener)

        recyMicSeats.layoutManager = GridLayoutManager(this, 3)//
        mMicSeatsAdapter.bindToRecyclerView(recyMicSeats)
        recyMicSeatSurfaces.layoutManager = GridLayoutManager(this, 3)//
        mMicSeatsSurfaceAdapter.bindToRecyclerView(recyMicSeatSurfaces)

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

    override fun getLayoutId(): Int {
        return R.layout.activity_amusement_room
    }

    class MySpanSizeLookup(val adapter: BaseQuickAdapter<*, *>) :
        GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            val type = adapter.getItemViewType(position)
            if (position == 0 || type == BaseQuickAdapter.FOOTER_VIEW) {
                return 3
            } else {
                return 1
            }
        }
    }

    class MyItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.bottom = 5
            outRect.left = 5
            outRect.right = 5
            outRect.top = 5
        }
    }

    inner class MicSeatsSurfaceAdapter : BaseQuickAdapter<LazySitUserMicSeat, BaseViewHolder>(
        R.layout.item_micseat_surface,
        ArrayList<LazySitUserMicSeat>()
    ) {
        override fun convert(helper: BaseViewHolder, item: LazySitUserMicSeat) {
            val lp = helper.itemView.qnSurfaceViewContainer.layoutParams as FrameLayout.LayoutParams
            if (data.indexOf(item) == 0) {
                lp.width = ViewUtil.dip2px(130f)
                lp.height = ViewUtil.dip2px(130f)

            } else {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT

            }
            helper.itemView.qnSurfaceViewContainer.layoutParams = lp
            val lp2 = helper.itemView.layoutParams as GridLayoutManager.LayoutParams
            if (data.indexOf(item) == 0) {
                lp2.bottomMargin = ViewUtil.dip2px(10f)
            } else {
                lp2.bottomMargin = ViewUtil.dip2px(0f)
            }
            helper.itemView.layoutParams = lp2
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            val manager = recyclerView.layoutManager
            if (manager is GridLayoutManager) {
                manager.spanSizeLookup = MySpanSizeLookup(this)
            }
            recyclerView.addItemDecoration(MyItemDecoration())
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

            val lp = helper.itemView.sqCard.layoutParams as FrameLayout.LayoutParams

            if (data.indexOf(item) == 0) {
                lp.width = ViewUtil.dip2px(130f)
                lp.height = ViewUtil.dip2px(130f)
            } else {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            helper.itemView.sqCard.layoutParams = lp

            val lp2 = helper.itemView.layoutParams as GridLayoutManager.LayoutParams
            if (data.indexOf(item) == 0) {
                lp2.bottomMargin = ViewUtil.dip2px(10f)
            } else {
                lp2.bottomMargin = ViewUtil.dip2px(0f)
            }
            helper.itemView.layoutParams = lp2
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
                helper.itemView.ivMicSeatAvatar.isVisible = !item.seat!!.isOpenVideo()
                helper.itemView.ivMicrophoneStatus.isSelected = item.seat!!.isOpenAudio()
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            val manager = recyclerView.layoutManager
            if (manager is GridLayoutManager) {
                manager.spanSizeLookup = MySpanSizeLookup(this)
            }
            recyclerView.addItemDecoration(MyItemDecoration())
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