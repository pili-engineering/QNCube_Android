package com.niucube.overhaul

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.DialogFragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.hapi.happy_dialog.FinalDialogFragment
import com.hapi.ut.AppCache
import com.hipi.vm.lazyVm
import com.niucube.overhaul.OverhaulVm.Companion.videoHeight
import com.niucube.overhaul.OverhaulVm.Companion.videoWidth
import com.niucube.overhaul.mode.OverhaulRoom
import com.pili.pldroid.player.widget.PLVideoView
import com.niucube.comp.mutabletrackroom.MicSeatListener
import com.niucube.comproom.RoomManager
import com.qiniu.comp.network.RetrofitManager
import com.niucube.rtcroom.mixstream.MixStreamManager
import com.niucube.comp.mutabletrackroom.MutableMicSeat
import com.qiniu.droid.audio2text.QNRtcAISdkManager
import com.qiniu.droid.whiteboard.QNWhiteBoard
import com.qiniu.droid.whiteboard.listener.QNAutoRemoveWhiteBoardListener
import com.qiniu.droid.whiteboard.model.Room
import com.qiniu.droid.whiteboard.model.RoomMember
import com.qiniu.jsonutil.JsonUtils
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.KeepLight
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.AppConfigService
import com.qiniudemo.baseapp.widget.CommonPagerAdapter
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.qiniudemo.baseapp.widget.EmptyFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_overhaul.*

@Route(path = RouterConstant.Overhaul.OverhaulRoom)
class OverhaulActivity : BaseActivity() {

    @Autowired
    @JvmField
    var overhaulRoomEntity: OverhaulRoom? = null

    @Autowired
    @JvmField
    var deviceMode: Int = OverhaulListActivity.deviceMode_common

    private val mOverhaulVm by lazyVm<OverhaulVm>()
    private val mMutableTrackRoom by lazy {
        mOverhaulVm.mMutableTrackRoom
    }

    companion object {
        init {
            QNWhiteBoard.init(AppCache.getContext(), true)
            QNRtcAISdkManager.init {
                var result = ""
                try {
                     result = RetrofitManager.create(AppConfigService::class.java)
                        .getToken(it).execute().body()?.token?:""
                }catch (e:Exception){
                    e.printStackTrace()
                }
                result
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun initViewData() {
        lifecycle.addObserver(KeepLight(this))
        if (deviceMode == OverhaulListActivity.deviceMode_Glasses
            ||
            deviceMode == OverhaulListActivity.deviceMode_Glasses_test
        ) {
            videoWidth = 960
            videoHeight = 540
            if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else {
            videoWidth = 540
            videoHeight = 960
            if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        }
        //房间封面
        Glide.with(this)
            .load(overhaulRoomEntity!!.roomInfo.image)
            .into(ivRoomCover)

        //白板监听
        QNWhiteBoard.addListener(object : QNAutoRemoveWhiteBoardListener {
            override fun onJoinSuccess(p0: Room, p1: RoomMember) {
                super.onJoinSuccess(p0, p1)
                //视频分辨率比屏幕小的情况下 写死的适配方案
                //白板缩放比和视频缩放比同步
                val wRatio = contentView.measuredWidth / videoWidth.toFloat()
                val hRatio = contentView.measuredHeight / videoHeight.toFloat()
                //缩放比例等于 视频宽高中最大的比例
                val whiteboardRatio = Math.max(wRatio, hRatio)
                // val whiteboardRatio = Math.min(wRatio, hRatio)
                val lp = whiteBoardView.layoutParams
                //扩大白板的宽宽 白板中心缩放
                lp.width = (videoWidth * whiteboardRatio).toInt()
                lp.height = (videoHeight * whiteboardRatio).toInt()
                whiteBoardView.layoutParams = lp
                //设置成为透明
                QNWhiteBoard.setBackgroundColor(0x00000000)
            }

            override fun onJoinFailed() {
                super.onJoinFailed()
                "加入白板房间失败".asToast()
                finish()
            }
        })

        //不同角色UI不同
        when (overhaulRoomEntity!!.role) {
            OverhaulRole.PROFESSOR.role -> {
                //专家
                textureRoomPlayer.visibility = View.VISIBLE
                plRoomPlayer.visibility = View.GONE
            }
            OverhaulRole.STAFF.role -> {
                textureRoomPlayer.visibility = View.VISIBLE
                plRoomPlayer.visibility = View.GONE
                whiteBoardView.setTouchAble(false)
            }

            OverhaulRole.STUDENT.role -> {
                whiteBoardView.setTouchAble(false)
                if (overhaulRoomEntity?.isStudentJoinRtc == true) {
                    textureRoomPlayer.visibility = View.VISIBLE
                    plRoomPlayer.visibility = View.GONE
                } else {
                    textureRoomPlayer.visibility = View.GONE
                    plRoomPlayer.visibility = View.VISIBLE
                    plRoomPlayer.displayAspectRatio = PLVideoView.ASPECT_RATIO_PAVED_PARENT

                }
            }
        }

        mOverhaulVm.mMutableTrackRoom.setAudiencePlayerView(plRoomPlayer)
        //注册麦位监听
        mMutableTrackRoom.addMicSeatListener(object : MicSeatListener {
            override fun onUserSitDown(micSeat: MutableMicSeat) {
                Log.d("MicSeatListener", "onUserSitDown ${JsonUtils.toJson(micSeat)}")
                //如果是检修员 只有检修员麦位有视频播放
                if (micSeat.userExtension?.userExtRoleType == OverhaulRole.STAFF.role) {
                    //设置检修员的预览窗口
                    mMutableTrackRoom.setUserCameraWindowView(
                        micSeat.uid,
                        textureRoomPlayer
                    )
                }
                //检修员负责混流
                if (overhaulRoomEntity?.role == OverhaulRole.STAFF.role) {
                    if (micSeat.userExtension?.userExtRoleType == OverhaulRole.STAFF.role) {
                        //设置需要检修员的视频混流参数
                        mMutableTrackRoom
                            .getMixStreamHelper()
                            .updateUserVideoMergeOptions(micSeat.uid,
                                MixStreamManager.MergeTrackOption().apply {
                                    mX = 0
                                    mY = 0
                                    mZ = 0
                                    mWidth = videoWidth
                                    mHeight = videoHeight
                                }
                            )
                        //设置需要检修员的音频
                        mMutableTrackRoom
                            .getMixStreamHelper()
                            .updateUserAudioMergeOptions(
                                micSeat.uid,
                                true
                            )
                    } else {
                        //专家只混语音
                        mMutableTrackRoom
                            .getMixStreamHelper()
                            .updateUserAudioMergeOptions(
                                micSeat.uid,
                                true
                            )
                    }
                }
            }

            override fun onUserSitUp(micSeat: MutableMicSeat, isOffLine: Boolean) {
                Log.d("MicSeatListener", "onUserSitUp ${JsonUtils.toJson(micSeat)} ${isOffLine}")
            }

            //麦位视频状态变化
            override fun onCameraStatusChanged(micSeat: MutableMicSeat) {
                Log.d(
                    "MicSeatListener",
                    "onTrackSeatVideoStatusChange ${JsonUtils.toJson(micSeat)} "
                )
            }

            override fun onMicAudioStatusChanged(micSeat: MutableMicSeat) {
                Log.d(
                    "MicSeatListener",
                    "onTrackSeatAudioStatusChange ${JsonUtils.toJson(micSeat)} "
                )
            }
        })

        view_pager.adapter = CommonPagerAdapter(
            listOf(OverhaulCoverFragment(), EmptyFragment()),
            supportFragmentManager
        )

        RxPermissions(this)
            .request(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
            .doFinally {
                //加入房间
                mOverhaulVm.enterRoom(overhaulRoomEntity!!)
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

    override fun onDestroy() {
        if (plRoomPlayer.isPlaying) {
            plRoomPlayer.pause()
        }
        super.onDestroy()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_overhaul
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