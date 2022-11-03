package com.niucube.module.ktv

import android.Manifest
import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.happy_dialog.FinalDialogFragment
import com.hipi.vm.backGround
import com.hipi.vm.lazyVm
import com.hipi.vm.lifecycleBg
import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.niucube.lrcview.LrcLoadUtils
import com.niucube.lrcview.bean.LrcData
import com.niucube.module.ktv.mode.Song
import com.qiniu.jsonutil.JsonUtils
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import kotlinx.android.synthetic.main.activity_ktvroom.*
import java.io.File
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.been.UserExtProfile
import com.qiniudemo.baseapp.been.isRoomHost
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.item_micseat_ktv.view.*
import androidx.recyclerview.widget.RecyclerView
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniudemo.baseapp.been.asBaseRoomEntity
import android.graphics.Rect
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.request.RequestOptions
import com.hapi.ut.ViewUtil
import com.niucube.bzuicomp.chatdialog.LightPubChatDialog
import com.niucube.comproom.ClientRoleType
import com.niucube.qrtcroom.ktvkit.KTVMusic
import com.niucube.qrtcroom.ktvkit.KTVPlayerListener
import com.niucube.qrtcroom.ktvkit.MusicTrack
import com.niucube.qrtcroom.ktvkit.TrackType
import com.niucube.qrtcroom.rtc.RoundTextureView
import com.qiniu.bzuicomp.bottominput.RoomInputDialog
import com.qiniu.bzuicomp.gift.GiftPanDialog
import com.qiniudemo.baseapp.KeepLight
import com.qiniudemo.baseapp.been.hostId
import com.qiniudemo.baseapp.widget.BlurTransformation
import com.qlive.beautyhook.BeautyHookerImpl
import com.qlive.uiwidghtbeauty.ui.EffectBeautyDialog
import com.qlive.uiwidghtbeauty.ui.StickerDialog

@Route(path = RouterConstant.KTV.KTVRoom)
class KTVRoomActivity : BaseActivity() {
    private val ktvRoomVm by lazyVm<KTVRoomVm>()

    @Autowired
    @JvmField
    var solutionType = ""

    @Autowired
    @JvmField
    var ktvRoomId = ""

    private val micSeatAdapter by lazy { MicSeatAdapter() }
    private val micSurfaceAdapter by lazy { MicSurfaceAdapter() }

    //聊天窗口
    private lateinit var pubChatDialog: LightPubChatDialog

    private val mEffectBeautyDialog by lazy { EffectBeautyDialog() }
    private val mStickerDialog by lazy { StickerDialog() }

    //播放状态监听
    private val mKTVPlayerListener by lazy {
        object : KTVPlayerListener<Song> {
            //播放出错
            override fun onError(errorCode: Int, msg: String) {
                msg.asToast()
                if (RoomManager.mCurrentRoom?.asBaseRoomEntity()?.isRoomHost() == true) {
                    playNext()
                }
            }

            //播放开始
            override fun onStart(ktvMusic: KTVMusic<Song>) {
                val song = ktvMusic.musicInfo ?: return
                Glide.with(this@KTVRoomActivity)
                    .load(song.image)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 8)))
                    .into(ivAlbum)
                Glide.with(this@KTVRoomActivity)
                    .load(song.image)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 8)))
                    .into(ivAlbumMin)

                tvCurrentSong.text = song.getMusicName()
                if (RoomManager.mCurrentRoom?.asBaseRoomEntity()?.isRoomHost() == true) {
                    ivPause.visibility = View.VISIBLE
                    ivNext.visibility = View.VISIBLE
                }

                lrcView.reset()
                lrcView.setLabel("${song.getMusicName()} 歌词加载中")

                ktvRoomVm.mKTVPlaylistsManager.loadMusicLirc(song) {
                    if (!it) {
                        val file = File(song.getTagDownLoadLocalUrl(TagDownLoadStatus.TagLrc))
                        val data: LrcData? = LrcLoadUtils.parse(file)
                        lrcView.setLrcData(data)
                    } else {
                        lrcView.setLabel("${song.getMusicName()} 歌词加载出错")
                    }
                }
                tvCurrentSong.text = song.name
                ivPause.isSelected = false
            }

            override fun onPause() {
                ivPause.isSelected = true
            }

            override fun onResume() {
                ivPause.isSelected = false
            }

            //播放进度
            override fun updatePosition(position: Long, duration: Long) {
                Log.d("QNAudioMixingManager", "updatePosition  " + position)
                lrcView.updateTime(position)
            }

            override fun onPlayCompleted() {
                if (RoomManager.mCurrentRoom?.asBaseRoomEntity()?.isRoomHost() == true) {
                    playNext()
                }
            }

            //音轨切换
            override fun onSwitchTrack(trackType: TrackType) {
            }
        }
    }

    //下一首
    private fun playNext() {
        lifecycleBg {
            doWork {
                val next =
                    ktvRoomVm.mKTVPlaylistsManager.getNext(ktvRoomVm.mKTVPlayerKit.mKTVMusic?.musicInfo)
                if (next == null) {
                    CommonTipDialog.TipBuild()
                        .setTittle("歌单空空的，快去添加!")
                        .setContent("开启耳返，佩戴耳机，效果更加哦～")
                        .isNeedCancelBtn(false)
                        .setPositiveText("我知道了")
                        .build()
                        .show(supportFragmentManager, "")
                    return@doWork
                } else {
                    ktvRoomVm.mKTVPlaylistsManager.loadMusicFile(next) {
                        if (!it) {
                            ktvRoomVm.mKTVPlayerKit.start(
                                UserInfoManager.getUserId(),
                                next.getMusicId(),
                                1,
                                listOf(
                                    MusicTrack(
                                        TrackType.accompany,
                                        next.getTagDownLoadLocalUrl(TagDownLoadStatus.TagAccompany)
                                    ),
                                    MusicTrack(
                                        TrackType.originVoice,
                                        next.getTagDownLoadLocalUrl(TagDownLoadStatus.TagOriginVoice)
                                    )
                                ),
                                next as Song
                            )
                        } else {
                            "加载音乐文件错误".asToast()
                        }
                    }
                }
            }
            catchError {
                it.message?.asToast()
                it.printStackTrace()
            }
        }
    }

    //麦位监听
    private val mMicSeatListener = object : UserMicSeatListener {
        /**
         * 有人上麦
         * @param micSeat
         */
        @SuppressLint("NotifyDataSetChanged")
        override fun onUserSitDown(micSeat: LazySitUserMicSeat) {
            micSeatAdapter.getLastSeat().let {
                val index = micSeatAdapter.data.indexOf(it)
                it.seat = micSeat
                micSeatAdapter.notifyItemChanged(micSeatAdapter.data.indexOf(it))
                micSurfaceAdapter.data[index].seat = micSeat
                val container = (micSurfaceAdapter.getViewByPosition(
                    index,
                    R.id.flSurfaceCotainer
                ) as ViewGroup?)
                container?.removeAllViews()
                container?.addView(
                    RoundTextureView(this@KTVRoomActivity).apply {
                        ktvRoomVm.mKtvRoom.setUserCameraWindowView(micSeat.uid, this)
                        setRadius(ViewUtil.dip2px(6f).toFloat())
                    },
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    )
                )
            }
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                ivMicrophone.visibility = View.VISIBLE
                ivBeauty.visibility = View.VISIBLE
                ivSticker.visibility = View.VISIBLE
                cbEnableEarMonitor.visibility = View.VISIBLE
            }
        }

        /**
         * 有人下麦
         * @param micSeat
         * @param isOffLine 是不是断线 否则主动下麦
         */
        @SuppressLint("NotifyDataSetChanged")
        override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {
            micSeatAdapter.getUserSeat(micSeat)?.let {
                val container = (micSurfaceAdapter.getViewByPosition(
                    micSeatAdapter.data.indexOf(it),
                    R.id.flSurfaceCotainer
                ) as ViewGroup?)
                container?.removeAllViews()
                val index = micSeatAdapter.data.indexOf(it)
                micSeatAdapter.remove(index)
                micSeatAdapter.addData(LazySitUserMicSeatWrap())
                micSeatAdapter.remove(index)
                micSeatAdapter.addData(LazySitUserMicSeatWrap())
            }
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                tvSelectSong.visibility = View.GONE
                ivMicrophone.visibility = View.GONE
            }
            if (micSeat.uid == RoomManager.mCurrentRoom?.asBaseRoomEntity()?.roomInfo?.creator) {
                "房主下线".asToast()
                ivCloseRoom.performClick()
            }
        }

        /**
         * 麦位麦克风变化
         * @param micSeat
         */
        override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {
            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                ivMicrophone.isSelected = !micSeat.isOpenAudio()
            }
            micSeatAdapter.getUserSeat(micSeat).let {
                micSeatAdapter.notifyItemChanged(micSeatAdapter.data.indexOf(it))
            }
        }

        override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {}

        override fun onSyncMicSeats(seats: List<LazySitUserMicSeat>) {
            seats.forEachIndexed { index, lazySitUserMicSeat ->
                micSeatAdapter.data[index].seat = lazySitUserMicSeat
                micSurfaceAdapter.data[index].seat = lazySitUserMicSeat
                (micSurfaceAdapter.getViewByPosition(
                    index,
                    R.id.flSurfaceCotainer
                ) as ViewGroup?)?.addView(
                    RoundTextureView(this@KTVRoomActivity).apply {
                        ktvRoomVm.mKtvRoom.setUserCameraWindowView(lazySitUserMicSeat.uid, this)
                        setRadius(ViewUtil.dip2px(6f).toFloat())
                    },
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    )
                )
                micSeatAdapter.notifyItemChanged(index)
            }
        }
    }

    //房间生命周期监听
    private val roomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomEntering(roomEntity: RoomEntity) {
            super.onRoomEntering(roomEntity)

            tvRoomTittle.text = roomEntity.asBaseRoomEntity().roomInfo?.title ?: ""
            tvMember.text = roomEntity.asBaseRoomEntity().roomInfo?.totalUsers ?: ""
            Glide.with(this@KTVRoomActivity).load(roomEntity.asBaseRoomEntity().roomInfo?.image)
                .into(ivHostAvatar)
        }

        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            ktvRoomVm.mKTVPlaylistsManager.headMusicChangedCall.invoke(Song())
            if (roomEntity.asBaseRoomEntity().isRoomHost()) {
                ivAccompany.visibility = View.VISIBLE
                tvSelectSong.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun initViewData() {
        lifecycle.addObserver(ktvRoomVm.mGiftTrackManager)
        lifecycle.addObserver(ktvRoomVm.mBigGiftManager)
        lifecycle.addObserver(ktvRoomVm.mDanmuTrackManager)
        ktvRoomVm.mBigGiftManager.attch(mBigGiftView)
        ktvRoomVm.mGiftTrackManager.addTrackView(giftShow1)
        ktvRoomVm.mGiftTrackManager.addTrackView(giftShow2)
        ktvRoomVm.mGiftTrackManager.addTrackView(giftShow3)
        ktvRoomVm.mDanmuTrackManager.addTrackView(danmu1)
        ktvRoomVm.mDanmuTrackManager.addTrackView(danmu2)
        lifecycle.addObserver(KeepLight(this))
        pubChatDialog = LightPubChatDialog(this)
        lrcView.setLabel("暂无音乐播放～")
        RoomManager.addRoomLifecycleMonitor(roomLifecycleMonitor)
        ktvRoomVm.mKTVPlayerKit.addKTVPlayerListener(mKTVPlayerListener)
        ktvRoomVm.mKtvRoom.addUserMicSeatListener(mMicSeatListener)
        ktvRoomVm.mKTVPlaylistsManager.headMusicChangedCall = {
            if (TextUtils.isEmpty(tvCurrentSong.text)) {
                tvCurrentSong.text = it.getMusicName()
                if (RoomManager.mCurrentRoom?.asBaseRoomEntity()?.isRoomHost() == true) {
                    ivNext.visibility = View.VISIBLE
                }
                ivNext.performClick()
            }
        }
        recyMicSeat.layoutManager = GridLayoutManager(this, 3)
        recyMicSeat.addItemDecoration(MyItemDecoration())
        micSeatAdapter.bindToRecyclerView(recyMicSeat)

        recySurface.layoutManager = GridLayoutManager(this, 3)
        recySurface.addItemDecoration(MyItemDecoration())
        micSurfaceAdapter.bindToRecyclerView(recySurface)

        ivPause.setOnClickListener {
            if (!ivPause.isSelected) {
                ktvRoomVm.mKTVPlayerKit.pause()
            } else {
                ktvRoomVm.mKTVPlayerKit.resume()
            }
        }
        ivAccompany.setOnClickListener {
            MusicSettingDialog().show(supportFragmentManager, "")
        }
        ivNext.setOnClickListener {
            playNext()
        }
        ivShowInput.setOnClickListener {
            pubChatDialog.setBackGround(com.niucube.bzuicomp.chatdialog.R.drawable.shape_pubchat_bg_eeeeee)
            pubChatDialog.show(supportFragmentManager, "")
        }
        ivCloseRoom.setOnClickListener {
            backGround {
                doWork {
                    ktvRoomVm.endRoom()
                }
                onFinally {
                    finish()
                }
            }
        }
        ivMicrophone.setOnClickListener {
            ktvRoomVm.mKtvRoom.muteLocalAudio(!ivMicrophone.isSelected)
        }
        tvSelectSong.setOnClickListener {
            SongsListDialog().show(supportFragmentManager, "")
        }
        cbEnableEarMonitor.setOnCheckedChangeListener { compoundButton, b ->
            //耳返
            ktvRoomVm.mKTVPlayerKit.enableEarMonitor(b)
        }

        var lastGiftId = ""
        ktvRoomVm.mGiftTrackManager.extGiftMsgCall = {
            if (it.sendGift.giftId != lastGiftId) {
                lastGiftId = it.sendGift.giftId
                ktvRoomVm.mBigGiftManager.playInQueen(it)
            }
        }
        ivDanmu.setOnClickListener {
            RoomInputDialog(2).apply {
                sendPubCall = {
                    ktvRoomVm.mDanmuTrackManager.buidMsg(it)
                }
            }
                .show(supportFragmentManager, "")
        }

        ivGift.setOnClickListener {
            GiftPanDialog().show(supportFragmentManager, "")
        }

        ivBeauty.setOnClickListener {
            mEffectBeautyDialog.show(supportFragmentManager, "")
        }

        ivSticker.setOnClickListener {
            mStickerDialog.show(supportFragmentManager, "")
        }

        RxPermissions(this)
            .request(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.CAMERA
            )
            .subscribe {
                if (it) {
                    //进入房间
                    ktvRoomVm.joinRoom(solutionType, ktvRoomId)
                } else {
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
        return R.layout.activity_ktvroom
    }

    class LazySitUserMicSeatWrap() {
        var seat: LazySitUserMicSeat? = null
    }

    inner class MicSurfaceAdapter : BaseQuickAdapter<LazySitUserMicSeatWrap, BaseViewHolder>(
        R.layout.item_micseat_surface_ktv,
        ArrayList<LazySitUserMicSeatWrap>().apply {
            for (i in 0..5) {
                add(LazySitUserMicSeatWrap())
            }
        }
    ) {
        override fun convert(helper: BaseViewHolder, item: LazySitUserMicSeatWrap) {
            helper.itemView.setOnClickListener {
                if (TextUtils.isEmpty(item.seat?.uid)) {
                    if (ktvRoomVm.mKtvRoom.mClientRole != ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                        ktvRoomVm.sitDown()
                    }
                }
            }
        }
    }

    //麦位UI
    inner class MicSeatAdapter : BaseQuickAdapter<LazySitUserMicSeatWrap, BaseViewHolder>(
        R.layout.item_micseat_ktv,
        ArrayList<LazySitUserMicSeatWrap>().apply {
            for (i in 0..5) {
                add(LazySitUserMicSeatWrap())
            }
        }
    ) {
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

        override fun convert(helper: BaseViewHolder, item: LazySitUserMicSeatWrap) {
            helper.itemView.setOnClickListener {
                if (TextUtils.isEmpty(item.seat?.uid)) {
                    if (ktvRoomVm.mKtvRoom.mClientRole != ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                        ktvRoomVm.sitDown()
                    }
                }
            }
            if (TextUtils.isEmpty(item.seat?.uid)) {
                helper.itemView.llFooter.visibility = View.VISIBLE
                helper.itemView.flContent.visibility = View.GONE
            } else {
                helper.itemView.llFooter.visibility = View.GONE
                helper.itemView.flContent.visibility = View.VISIBLE
                if (item.seat?.uid == RoomManager.mCurrentRoom?.hostId()) {
                    helper.itemView.tvHostFlag.visibility = View.VISIBLE
                } else {
                    helper.itemView.tvHostFlag.visibility = View.GONE
                }
                item.seat?.userExtension?.userExtProfile?.let { it ->
                    val userExtProfile = JsonUtils.parseObject(it, UserExtProfile::class.java)
                    userExtProfile?.let {
                        Glide.with(mContext)
                            .load(it.avatar)
                            .into(helper.itemView.ivCover)
                        helper.itemView.tvName.text = it.name
                    }
                }
                helper.itemView.ivMicrophoneStatus.isSelected = item.seat!!.isOpenAudio()
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
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

    class MyItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.bottom = 10
            outRect.left = 10
            outRect.right = 10
            outRect.top = 10
        }
    }
}