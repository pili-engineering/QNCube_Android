package com.niucube.overhaul

import android.graphics.*
import android.util.Log
import android.view.View
import com.blabla.sreenshot.ScreenShotPlugin
import com.hipi.vm.activityVm
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.bzuicomp.pubchat.PubChatAdapter
import com.qiniu.bzuicomp.pubchat.PubChatMsgManager
import com.qiniu.bzuicomp.pubchat.PubChatMsgModel
import com.niucube.mutabletrackroom.MicSeatListener
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.mutabletrackroom.MutableMicSeat
import com.niucube.overhaul.fbo.AicCircuitBoardFrameListener
import com.qiniu.droid.audio2text.QNAudioToTextAnalyzer
import com.qiniu.droid.audio2text.audio.QNAudioToText
import com.qiniu.droid.audio2text.audio.QNAudioToTextParam
import com.qiniu.droid.rtc.QNCameraVideoTrack
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.util.MediaStoreUtils
import kotlinx.android.synthetic.main.fragment_overhaul_cover.*


class OverhaulCoverFragment : BaseFragment() {

    private val mOverhaulVm by activityVm<OverhaulVm>()
    private val mWelComeReceiver by lazy {
        mOverhaulVm.mWelComeReceiver
    }
    private val mMutableTrackRoom by lazy {
        mOverhaulVm.mMutableTrackRoom
    }
    private var mQNAudioToTextAnalyzer: QNAudioToTextAnalyzer? = null

    private val mMicSeatListener by lazy {
        object : MicSeatListener {
            override fun onUserSitDown(micSeat: MutableMicSeat) {}
            override fun onUserSitUp(micSeat: MutableMicSeat, isOffLine: Boolean) {}
            override fun onCameraStatusChanged(micSeat: MutableMicSeat) {
                if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                    flCloseVideo.isSelected = !micSeat.isOwnerOpenVideo
                }
            }

            override fun onMicAudioStatusChanged(micSeat: MutableMicSeat) {
                if (micSeat.isMySeat(UserInfoManager.getUserId())) {
                    flCloseAudio.isSelected = !micSeat.isOwnerOpenAudio
                }
            }
        }
    }

    /**
     * 语音识别监听
     */
    private val audio2TextListener by lazy {
        object :
            QNAudioToTextAnalyzer.QNAudioToTextCallback {
            override fun onStart() {
                Log.d(
                    "TranslateWebSocket",
                    "onStartonStartonStart "
                )
                tvVoiceCommandTip?.setTextColor(Color.parseColor("#ffffff"))
            }

            override fun onError(code: Int, msg: String?) {
                // switchAudio2Text?.isChecked = false
//                tvVoiceCommandTip?.setTextColor(Color.parseColor("#000000"))
//                if (!isDestroy) {
//                    startQNAudioToTextAnalyzer()
//                }
            }

            override fun onStop() {
                Log.d(
                    "TranslateWebSocket",
                    "onStoponStoponStoponStop "
                )
                tvVoiceCommandTip?.setTextColor(Color.parseColor("#000000"))
                //switchAudio2Text?.isChecked = false
                if (!isDestroy) {
                    startQNAudioToTextAnalyzer()
                }
            }

            override fun onAudioToText(audioToText: QNAudioToText) {
                if (audioToText.finalX != 1) {
                    return
                }
                Log.d(
                    "TranslateWebSocket",
                    "onAudioToText ${audioToText.transcript} ${audioToText.endSeq} ${audioToText.finalX}"
                )
                var deal = false

                val msgText = audioToText.transcript

                if(msgText.contains("关闭麦克风")&&msgText.length<10){
                    deal = true
                    mMutableTrackRoom.muteLocalAudio(true)
                }else if(msgText.contains("打开麦克风")&&msgText.length<10){
                    deal = true
                    mMutableTrackRoom.muteLocalAudio(false)
                }else  if(msgText.contains("关闭摄像头")&&msgText.length<10){
                    deal = true
                    mMutableTrackRoom.muteLocalVideo(true)
                }
                else  if(msgText.contains("打开摄像头")&&msgText.length<10){
                    deal = true
                    mMutableTrackRoom.muteLocalVideo(false)
                }
                else  if(msgText.contains("结束通话")&&msgText.length<6){
                    deal = true
                    mOverhaulVm.endRoom{
                       activity?.finish()
                    }
                }else if(msgText=="截图。"){
                    deal = true
                    ScreenShotPlugin.getInstance().startMediaRecorder(requireActivity(),
                        object : ScreenShotPlugin.OnScreenShotListener {
                            override fun onFinish(bm: Bitmap?) {
                                bm?.let {
                                    "截图成功".asToast()
                                    MediaStoreUtils.saveImageToGallery(bm, requireContext())
                                }
                            }

                            override fun onError(code: Int, msg: String?) {
                                msg?.asToast()
                            }
                        })
                }

                if(deal){
                    PubChatMsgManager.onNewMsg(PubChatMsgModel().apply {
                        senderId = UserInfoManager.getUserId()
                        senderName = "语音指令"
                        msgContent = "${audioToText.transcript} -> ${if (deal) "识别成功" else "未识别"}"
                    })
                }
            }
        }
    }

    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            mWelComeReceiver.sendEnterMsg()
        }
    }

    private fun startQNAudioToTextAnalyzer() {
        Log.d("TranslateWebSocket", "code startQNAudioToTextAnalyzer ")
        if( mMutableTrackRoom.getUserAudioTrackInfo(UserInfoManager.getUserId())==null){
            Log.d("TranslateWebSocket", "code getUserAudioTrackInfo == null")
            return
        }
        mQNAudioToTextAnalyzer = QNAudioToTextAnalyzer.start(
            mMutableTrackRoom.getUserAudioTrackInfo(UserInfoManager.getUserId()),
            QNAudioToTextParam().apply {
                hotWords = "关闭麦克风,1;打开麦克风,1;结束通话,1;关闭摄像头,1;打开摄像头,1;截图,1"
            },
            audio2TextListener
        )
    }

    private val mQNVideoFrameListener by lazy { AicCircuitBoardFrameListener() }

    override fun initViewData() {

        mMutableTrackRoom.addMicSeatListener(object : MicSeatListener {
            override fun onUserSitDown(micSeat: MutableMicSeat) {
                if ((micSeat.isMySeat(UserInfoManager.getUserId()) && mOverhaulVm.overhaulRoomEntity?.role == OverhaulRole.STAFF.role)
                ) {
                    val localVideo =
                        mMutableTrackRoom.getUserVideoTrackInfo(UserInfoManager.getUserId()) as QNCameraVideoTrack
                    localVideo.setVideoFrameListener(mQNVideoFrameListener)
                }
            }

            override fun onUserSitUp(micSeat: MutableMicSeat, isOffLine: Boolean) {}
            override fun onCameraStatusChanged(micSeat: MutableMicSeat) {}
            override fun onMicAudioStatusChanged(micSeat: MutableMicSeat) {
                if ((micSeat.isMySeat(UserInfoManager.getUserId()) && mOverhaulVm.overhaulRoomEntity?.role == OverhaulRole.STAFF.role) &&
                    mQNAudioToTextAnalyzer == null
                ) {
                    startQNAudioToTextAnalyzer()
                }
            }
        })

        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
        pubChatView.setAdapter(PubChatAdapter())
        lifecycle.addObserver(mWelComeReceiver)
        lifecycle.addObserver(pubChatView)
        mMutableTrackRoom.addMicSeatListener(mMicSeatListener)

        //白板操作面板选择需要的功能
        whiteBoardControl.setIsPenNeed(true)
        whiteBoardControl.setIsMarkNeed(true)
        whiteBoardControl.setIsEraserNeed(true)
        whiteBoardControl.setIsGeometryNeed(false)
        whiteBoardControl.setIsSelectNeed(false)
        whiteBoardControl.setIsInsertFileNeed(false)
        whiteBoardControl.setIsLaserNeed(false)
        whiteBoardControl.setIsRestoreNeed(false)

        roomTittle.text = mOverhaulVm.overhaulRoomEntity?.roomInfo?.title
//        ivSpeeker.setOnClickListener {
//            mMutableTrackRoom.muteAllRemoteAudio(!ivSpeeker.isSelected)
//            ivSpeeker.isSelected = !ivSpeeker.isSelected
//        }
        flCloseAudio.setOnClickListener {
            mMutableTrackRoom.muteLocalAudio(!flCloseAudio.isSelected)
        }
        flCloseVideo.setOnClickListener {
            mMutableTrackRoom.muteLocalVideo(!flCloseVideo.isSelected)
        }
        flHangup.setOnClickListener {
            mWelComeReceiver.sendQuitMsg()
            mOverhaulVm.endRoom{
                activity?.finish()
            }
        }
        ivSwitch.setOnClickListener {
            mMutableTrackRoom.switchCamera()
        }
        when (mOverhaulVm.overhaulRoomEntity?.role) {
            OverhaulRole.STAFF.role -> {
                // switchAudio2Text.visibility = View.VISIBLE
                ivSwitch.visibility = View.VISIBLE
                tvVoiceCommandTip.visibility = View.VISIBLE
            }
            OverhaulRole.PROFESSOR.role -> {
                whiteBoardControl.visibility = View.VISIBLE
                flCloseVideo.visibility = View.GONE
                ivSwitch.visibility = View.GONE
                tvVoiceCommandTip.visibility = View.GONE
            }
            OverhaulRole.STUDENT.role -> {
                flCloseVideo.visibility = View.GONE
                flCloseAudio.visibility = View.GONE
                ivSwitch.visibility = View.GONE
                tvVoiceCommandTip.visibility = View.GONE
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_overhaul_cover
    }

    private var isDestroy = false
    override fun onDestroy() {
        isDestroy = true
        super.onDestroy()
        mQNAudioToTextAnalyzer?.stop()
    }
}