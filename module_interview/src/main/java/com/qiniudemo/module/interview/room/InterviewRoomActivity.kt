package com.qiniudemo.module.interview.room


import android.Manifest
import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.hapi.happy_dialog.FinalDialogFragment
import com.hipi.vm.lazyVm
import com.niucube.comp.mutabletrackroom.MicSeatListener
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.ext.asToast
import com.qiniu.bzcomp.user.UserInfoManager
import com.niucube.rtcroom.screencapture.ScreenMicSeatListener
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.comproom.provideMeId
import com.niucube.rtcroom.screencapture.ScreenCapturePlugin
import com.niucube.absroom.seat.ScreenMicSeat
import com.niucube.comp.mutabletrackroom.MutableMicSeat
import com.qiniu.droid.rtc.QNScreenVideoTrack
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.qiniudemo.module.interview.R
import com.qiniudemo.module.interview.been.InterviewRoomModel
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.interview_activity_interview_room.*

@Route(path = RouterConstant.Interview.InterviewRoom)
class InterviewRoomActivity : BaseActivity() {

    /**
     * 房间业务
     */
    private val mInterviewRoomVm by lazyVm<InterviewRoomVm>()

    /**
     * 面试ID
     */
    @Autowired
    @JvmField
    var interviewId = ""

    /**
     * 面试房间引擎
     */
    private val mInterviewRoom by lazy {
        mInterviewRoomVm.mInterviewRoom
    }

    private lateinit var bigSurfaceFront: InterviewSurfaceView
    private lateinit var bigSurfaceBack: InterviewSurfaceView
    private lateinit var bigSurfaceFrontContainer: FrameLayout
    private lateinit var bigSurfaceBackContainer: FrameLayout

    /**
     * 麦位监听
     */
    private val mTrackSeatListener by lazy {
        object : MicSeatListener {
            override fun onUserSitDown(targetSeat: MutableMicSeat) {
                if (!targetSeat.isMySeat()) {
                    checkIsRoomOnlyMe()
                }
                //我的麦位
                if (targetSeat.isMySeat()) {
                    smallSurfaceView.onSeatDown(mInterviewRoom, targetSeat)
                } else {
                    bigSurfaceBack.onSeatDown(mInterviewRoom, targetSeat)
                }
            }

            override fun onUserSitUp(targetSeat: MutableMicSeat, isOffLine: Boolean) {
                if (targetSeat.isMySeat()) {
                    smallSurfaceView.onSeatLeave(mInterviewRoom, targetSeat)
                } else {
                    bigSurfaceBack.onSeatLeave(mInterviewRoom, targetSeat)
                }
                //如果是房主（面试官）
                checkIsRoomOnlyMe()
            }

            override fun onCameraStatusChanged(targetSeat: MutableMicSeat) {

                if (targetSeat.isMySeat()) {
                    smallSurfaceView.onTrackStatusChange(mInterviewRoom, targetSeat)
                } else {
                    bigSurfaceBack.onTrackStatusChange(mInterviewRoom, targetSeat)
                }
            }

            override fun onMicAudioStatusChanged(targetSeat: MutableMicSeat) {
                if (targetSeat.isMySeat()) {
                    smallSurfaceView.onTrackStatusChange(mInterviewRoom, targetSeat)
                } else {
                    bigSurfaceBack.onTrackStatusChange(mInterviewRoom, targetSeat)
                }
            }
        }
    }

    //屏幕共享监听
    private var mScreenMicSeatListener = object : ScreenMicSeatListener {
        override fun onScreenMicSeatAdd(targetSeat: ScreenMicSeat) {
            //屏幕轨道
            bigSurfaceFront.onScreenSeatAdd(mInterviewRoom, targetSeat)
            onScreenTrackOn(targetSeat)
            btScree.isSelected = true
        }

        override fun onScreenMicSeatRemove(targetSeat: ScreenMicSeat) {
            bigSurfaceFront.onScreenSeatRemoved(mInterviewRoom, targetSeat)
            onScreenTrackOff()
            btScree.isSelected = false
            checkIsRoomOnlyMe()
        }

    }

    /**
     * 房间生命周期监听
     */
    private val roomMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            val roomToken = (roomEntity as InterviewRoomModel)
            roomToken.allUserList?.forEach {
                if (it.accountId == roomEntity.provideMeId()) {
                    smallSurfaceView.setUserInfo(it)
                } else {
                    bigSurfaceBack.setUserInfo(it)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun initViewData() {
        RoomManager.addRoomLifecycleMonitor(roomMonitor)
        // 轨道回调监听
        mInterviewRoom.addMicSeatListener(mTrackSeatListener)
        mInterviewRoom.getScreenShareManager().addScreenMicSeatListener(mScreenMicSeatListener)
        val trans = supportFragmentManager.beginTransaction()
        trans.replace(R.id.flCover, RoomCover())
        trans.commit()
        trackVp.adapter = BigSurfaceViewAdapter()
        trackVp.offscreenPageLimit = 2
        onScreenTrackOff()
        smallSurfaceView.setIsTop(true)
        smallSurfaceViewParent.setOnClickListener {
            swapSurfaceView()
        }
        smallSurfaceViewParent.post {
            checkIsRoomOnlyMe()
        }

        lifecycle.addObserver(smallSurfaceView)
        trackVp.post {
            lifecycle.addObserver(bigSurfaceBack)
            lifecycle.addObserver(bigSurfaceFront)
        }

        btScree.setOnClickListener {
            if (btScree.isSelected) {
                if (bigSurfaceFront.mTargetSeat?.uid == UserInfoManager.getUserId()) {
                    mInterviewRoom.getScreenShareManager().unPubLocalScreenTrack()
                } else {

                }
            } else {
                if (bigSurfaceFront.mTargetSeat != null) {
                    "正在共享屏幕".asToast()
                }
                if (!QNScreenVideoTrack.isScreenCaptureSupported()) {
                    "当前设备不支持屏幕共享".asToast()
                }
                mInterviewRoom.getScreenShareManager().pubLocalScreenTrackWithPermissionCheck(this, object : ScreenCapturePlugin.ScreenCaptureListener {
                    override fun onSuccess() {}
                    override fun onError(code: Int, msg: String?) {}
                })
            }
        }

        trackVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> rgIndicator.check(R.id.rbFront)
                    1 -> rgIndicator.check(R.id.rbBack)
                }
            }
        })
        rgIndicator.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbFront -> {
                    if (trackVp.currentItem != 0) {
                        trackVp.currentItem = 0
                    }
                }
                R.id.rbBack -> {
                    if (trackVp.currentItem != 1) {
                        trackVp.currentItem = 1
                    }
                }
            }
        }
        RxPermissions(this)
            .request(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
            .subscribe {
                if (it) {
                    //进入房间
                    mInterviewRoomVm.enterRoom(interviewId)
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

    //交换两个视频
    private fun swapSurfaceView() {
        val smallView: InterviewSurfaceView =
            smallSurfaceViewParent.getChildAt(0) as InterviewSurfaceView
        val bigView: InterviewSurfaceView = if (trackVp.currentItem == 0)
            bigSurfaceFrontContainer.getChildAt(0) as InterviewSurfaceView
        else
            bigSurfaceBackContainer.getChildAt(0) as InterviewSurfaceView
        val lpBig = bigView.layoutParams
        val lpSmall = smallView.layoutParams
        smallView.layoutParams = lpBig
        bigView.layoutParams = lpSmall
        val parentSmall = smallView.parent as ViewGroup
        parentSmall.removeView(smallView)
        val parentBig = bigView.parent as ViewGroup
        parentBig.removeView(bigView)
        parentBig.addView(smallView)
        parentSmall.addView(bigView)
        bigView.setIsTop(true)
        smallView.setIsTop(false)
    }

    /**
     * 屏幕采集轨道打开
     */
    private fun onScreenTrackOn(targetSeat: ScreenMicSeat) {
        if (!targetSeat.isMySeat()) {
            trackVp.currentItem = 0
            bigSurfaceFront.setCover(0)
            if (!targetSeat.isMySeat()) {
                trackVp.isUserInputEnabled = true
                rgIndicator.visibility = View.VISIBLE
            } else {
                trackVp.isUserInputEnabled = false
                rgIndicator.visibility = View.GONE
            }
        }
    }

    /**
     * 屏幕采集轨道关闭
     */
    private fun onScreenTrackOff() {
        trackVp.currentItem = 1
        trackVp.isUserInputEnabled = false
        rgIndicator.visibility = View.GONE
    }

    private fun resetSwap() {
        val smv = smallSurfaceView
        smallSurfaceViewParent.removeViewAt(0)
        bigSurfaceBackContainer.removeViewAt(0)
        bigSurfaceFrontContainer.removeViewAt(0)
        smallSurfaceViewParent.addView(smv)
        bigSurfaceBackContainer.addView(bigSurfaceBack)
        bigSurfaceFrontContainer.addView(bigSurfaceFront)
        smallSurfaceView.setIsTop(true)
        bigSurfaceBack.setIsTop(false)
        bigSurfaceFront.setIsTop(false)

    }

    private fun checkIsRoomOnlyMe() {

        var isAllMe = true
        mInterviewRoom.mMicSeats.forEach {
            if (!it.isMySeat()) {
                isAllMe = false
            }
        }
        if (mInterviewRoom.mMicSeats.isEmpty() || isAllMe) {

            if (bigSurfaceBackContainer.getChildAt(0) == smallSurfaceView
                && bigSurfaceFrontContainer.getChildAt(0) == bigSurfaceFront
                && smallSurfaceViewParent.getChildAt(0) == bigSurfaceBack
            ) {
                smallSurfaceView.visibility = View.VISIBLE
                bigSurfaceFront.visibility = View.GONE
                bigSurfaceBack.visibility = View.GONE
                return
            }
            if (bigSurfaceBackContainer.getChildAt(0) != bigSurfaceBack
                || bigSurfaceFrontContainer.getChildAt(0) != bigSurfaceFront
                || smallSurfaceViewParent.getChildAt(0) != smallSurfaceView

            ) {
                resetSwap()
            }
            swapSurfaceView()
            smallSurfaceView.visibility = View.VISIBLE
            bigSurfaceFront.visibility = View.GONE
            bigSurfaceBack.visibility = View.GONE
        } else {
            resetSwap()
            smallSurfaceView.visibility = View.VISIBLE
            bigSurfaceFront.visibility = View.VISIBLE
            bigSurfaceBack.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RoomManager.removeRoomLifecycleMonitor(roomMonitor)
        mInterviewRoom.closeRoom()
    }

    override fun isToolBarEnable(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.interview_activity_interview_room
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

    override fun onPause() {
        super.onPause()
        if (RoomManager.mCurrentRoom?.isJoined == true) {
            streamerSwitchToBackstage()
        }
    }

    override fun onResume() {
        super.onResume()
        if (RoomManager.mCurrentRoom?.isJoined == true) {
            streamerBackToLiving()
        }
    }

    private fun streamerSwitchToBackstage() {
        //设置后台推流背景
//        val image = QNImage(this)
//        image.setResourceId(R.drawable.interview_pause_publish)
//        mInterviewRoom.pushCameraTrackWithImage(image)
    }

    private fun streamerBackToLiving() {
        //   mInterviewRoom.pushCameraTrackWithImage(null)
    }


    inner class BigSurfaceViewAdapter :
        RecyclerView.Adapter<BigSurfaceViewAdapter.ViewPagerViewHolder>() {

        inner class ViewPagerViewHolder(private val interviewSurfaceView: View) :
            RecyclerView.ViewHolder(interviewSurfaceView) {
            var flBigTrackContainer =
                interviewSurfaceView.findViewById<FrameLayout>(R.id.flBigTrackContainer)
            var bigSurfaceView =
                interviewSurfaceView.findViewById<InterviewSurfaceView>(R.id.bigSurfaceView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
            return ViewPagerViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.interview_item_surface_track, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return 2
        }

        override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
            holder.bigSurfaceView.setIsTop(false)
            if (position == 0) {
                bigSurfaceFrontContainer = holder.flBigTrackContainer
                bigSurfaceFront = holder.bigSurfaceView
            } else {
                bigSurfaceBackContainer = holder.flBigTrackContainer
                bigSurfaceBack = holder.bigSurfaceView
            }
        }
    }
}