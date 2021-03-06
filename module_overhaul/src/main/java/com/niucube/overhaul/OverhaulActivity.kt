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
import com.niucube.basemutableroom.mixstream.MixStreamManager
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
        //????????????
        Glide.with(this)
            .load(overhaulRoomEntity!!.roomInfo.image)
            .into(ivRoomCover)

        //????????????
        QNWhiteBoard.addListener(object : QNAutoRemoveWhiteBoardListener {
            override fun onJoinSuccess(p0: Room, p1: RoomMember) {
                super.onJoinSuccess(p0, p1)
                //??????????????????????????????????????? ?????????????????????
                //???????????????????????????????????????
                val wRatio = contentView.measuredWidth / videoWidth.toFloat()
                val hRatio = contentView.measuredHeight / videoHeight.toFloat()
                //?????????????????? ??????????????????????????????
                val whiteboardRatio = Math.max(wRatio, hRatio)
                // val whiteboardRatio = Math.min(wRatio, hRatio)
                val lp = whiteBoardView.layoutParams
                //????????????????????? ??????????????????
                lp.width = (videoWidth * whiteboardRatio).toInt()
                lp.height = (videoHeight * whiteboardRatio).toInt()
                whiteBoardView.layoutParams = lp
                //??????????????????
                QNWhiteBoard.setBackgroundColor(0x00000000)
            }

            override fun onJoinFailed() {
                super.onJoinFailed()
                "????????????????????????".asToast()
                finish()
            }
        })

        //????????????UI??????
        when (overhaulRoomEntity!!.role) {
            OverhaulRole.PROFESSOR.role -> {
                //??????
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
        //??????????????????
        mMutableTrackRoom.addMicSeatListener(object : MicSeatListener {
            override fun onUserSitDown(micSeat: MutableMicSeat) {
                Log.d("MicSeatListener", "onUserSitDown ${JsonUtils.toJson(micSeat)}")
                //?????????????????? ????????????????????????????????????
                if (micSeat.userExtension?.userExtRoleType == OverhaulRole.STAFF.role) {
                    //??????????????????????????????
                    mMutableTrackRoom.setUserCameraWindowView(
                        micSeat.uid,
                        textureRoomPlayer
                    )
                }
                //?????????????????????
                if (overhaulRoomEntity?.role == OverhaulRole.STAFF.role) {
                    if (micSeat.userExtension?.userExtRoleType == OverhaulRole.STAFF.role) {
                        //??????????????????????????????????????????
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
                        //??????????????????????????????
                        mMutableTrackRoom
                            .getMixStreamHelper()
                            .updateUserAudioMergeOptions(
                                micSeat.uid,
                                true
                            )
                    } else {
                        //??????????????????
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

            //????????????????????????
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
                //????????????
                mOverhaulVm.enterRoom(overhaulRoomEntity!!)
            }
            .subscribe {
                if (!it) {
                    CommonTipDialog.TipBuild()
                        .setContent("?????????????????????")
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

    //???????????????????????????
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK
            && RoomManager.mCurrentRoom?.isJoined == true
        ) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}