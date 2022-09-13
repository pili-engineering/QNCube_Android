package com.niucube.module.videowatch

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.hapi.happy_dialog.FinalDialogFragment
import com.hipi.vm.createVm
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
//import com.niucube.compui.beauty.SenseTimePluginManager
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.niucube.module.videowatch.mode.Movie
import com.niucube.player.PlayerStatus
import com.niucube.player.PlayerStatusListener
import com.pili.pldroid.player.AVOptions
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonPagerAdapter
import com.qiniudemo.baseapp.widget.CommonTipDialog
import kotlinx.android.synthetic.main.activity_video_watch_room.*

@Route(path = RouterConstant.VideoRoom.VideoRoom)
class VideoWatchRoomActivity : BaseActivity() {
    private val roomVm by createVm<VideoRoomVm>()
    private val videoSourceVm by createVm<VideoSourceVm>()

    @Autowired
    @JvmField
    var movie: Movie? = null
    @Autowired
    @JvmField
    var solutionType = ""
    @Autowired
    @JvmField
    var roomId = ""
    @Autowired
    @JvmField
    var joinInvitationCode=""

    private val fragments = listOf<Fragment>(ChatFragment().apply {
        goInviteOnlineUserPageCall = {
            val fragment = OnlineUserFragment.newInstance(OnlineUserFragment.option_type_invite)
            fragment.backCall = {
                removeOptionFragment(fragment)
            }
            replaceOptionFragment(fragment)
        }
        goManagerOnlineUserPageCall = {
            val fragment = OnlineUserFragment.newInstance(OnlineUserFragment.option_type_manager)
            fragment.backCall = {
                removeOptionFragment(fragment)
            }
            replaceOptionFragment(fragment)
        }
        goOnlineUserPageCall = {
            val fragment = OnlineUserFragment.newInstance(OnlineUserFragment.option_type_view)
            fragment.backCall = {
                removeOptionFragment(fragment)
            }
            replaceOptionFragment(fragment)
        }
        goMovieListPageCall = {
            val fragment = MovieListFragment.newInstance()
            fragment.backCall = {
                removeOptionFragment(fragment)
            }
            replaceOptionFragment(fragment)
        }
        goMicSeatPageCall = {
            this@VideoWatchRoomActivity.vpPager.currentItem = 1
        }
    }, MicSeatFragment().apply {
        backCall = { this@VideoWatchRoomActivity.vpPager.currentItem = 0 }
        goInviteOnlineUserPageCall = {
            val fragment = OnlineUserFragment.newInstance(OnlineUserFragment.option_type_invite)
            fragment.backCall = {
                removeOptionFragment(fragment)
            }
            replaceOptionFragment(fragment)
        }
    })

    private val mPlayerStatusListener = object : PlayerStatusListener {
        private var currentRTCPubID = ""
        override fun onPlayStateChanged(status: Int) {
            if (status == PlayerStatus.STATE_COMPLETED) {
                videoSourceVm.nextMovie(roomVm.mMovieSignaler.mCurrentMovieSignal?.movieInfo)
            }
        }

        override fun onPlayModeChanged(model: Int) {
            if (PlayerStatus.MODE_FULL_SCREEN == model) {
                enterFullScreen()
            }
            if (PlayerStatus.MODE_NORMAL == model) {
                exitFullScreen()
            }
            (fragments.get(1) as MicSeatFragment).onPlayModeChanged(model)
        }
    }

    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            mVideoView.init(roomEntity.isRoomHost(), roomEntity.provideRoomToken())
        }
    }

    private val mQNVideoFrameListener by lazy {
        BeautyVideoFrameListener().apply {
            lifecycle.addObserver(this)
        }
    }

    private val micSeatListener = object : UserMicSeatListener {
        override fun onUserSitDown(micSeat: LazySitUserMicSeat) {
            if ((RoomManager.mCurrentRoom?.isRoomHost() == true &&
                        !micSeat.isMySeat(UserInfoManager.getUserId())
                        ) ||
                (RoomManager.mCurrentRoom?.isRoomHost() == false
                        && micSeat.isMySeat(UserInfoManager.getUserId())
                        )
            ) {
                vpPager.currentItem = 1
            }
//            if (micSeat.isMySeat(UserInfoManager.getUserId())) {
//                mVideoView.post {
//                    val localVideo =
//                        roomVm.mRtcRoom.getUserVideoTrackInfo(UserInfoManager.getUserId()) as QNCameraVideoTrack
//                    localVideo.setVideoFrameListener(mQNVideoFrameListener)
//                }
//            }
        }

        override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {}
        override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {}
        override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {}
    }

    private fun replaceOptionFragment(fragment: Fragment) {
        val trans = supportFragmentManager.beginTransaction()
        trans.replace(R.id.flOptionContainer, fragment)
        trans.commit()
    }

    private fun removeOptionFragment(fragment: Fragment) {
        val trans = supportFragmentManager.beginTransaction()
        trans.remove(fragment)
        trans.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if(RoomManager.mCurrentRoom!=null){
            finish()
            "房间清理中，请稍后".asToast()
            return
        }
        super.onCreate(savedInstanceState)
    }

    override fun initViewData() {
       // SenseTimePluginManager.initEffect(AppCache.getContext())
        mVideoView.setMovieSignaler(roomVm.mMovieSignaler)
        mVideoView.setRtcPubService(roomVm.mRtcPubService)
        videoSourceVm.mCurrentMovieOptionChangeCall = {
            mVideoView.setUp(it)
            mVideoView.startPlay()
        }
        roomVm.mRtcRoom.addUserMicSeatListener(micSeatListener)
        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
        lifecycle.addObserver(mVideoView)
        mVideoView.setPlayerConfig(mVideoView.getPlayerConfig().setAVOptions(AVOptions().apply {
            setInteger(AVOptions.KEY_FAST_OPEN, 1);
            setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 5);
            setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
            // setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_AUTO);
            setString(AVOptions.KEY_CACHE_DIR, cacheDir.absolutePath)
            setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_SW_DECODE)
        }))
        mVideoView.addPlayStatusListener(mPlayerStatusListener, true)
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            enterFullScreen()
        }
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            exitFullScreen()
        }
        vpPager.adapter = CommonPagerAdapter(fragments, supportFragmentManager)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_video_watch_room
    }

    fun enterFullScreen() {
        llVideoActContainer.orientation = HORIZONTAL

        val lpVideo = videoViewContainer.layoutParams as LinearLayout.LayoutParams
        val lpVp = flFragmentContainer.layoutParams as LinearLayout.LayoutParams

        lpVideo.width = 0
        lpVideo.height = ViewGroup.LayoutParams.MATCH_PARENT
        lpVideo.weight = 2f

        lpVp.width = 0
        lpVp.height = ViewGroup.LayoutParams.MATCH_PARENT
        lpVp.weight = 1f

        videoViewContainer.layoutParams = lpVideo
        flFragmentContainer.layoutParams = lpVp
    }

    fun exitFullScreen() {
        llVideoActContainer.orientation = VERTICAL
        val lpVideo = videoViewContainer.layoutParams as LinearLayout.LayoutParams
        val lpVp = flFragmentContainer.layoutParams as LinearLayout.LayoutParams
        lpVideo.width = ViewGroup.LayoutParams.MATCH_PARENT
        lpVideo.height = ViewGroup.LayoutParams.WRAP_CONTENT
        lpVideo.weight = 0f
        lpVp.width = ViewGroup.LayoutParams.MATCH_PARENT
        lpVp.height = ViewGroup.LayoutParams.MATCH_PARENT
        lpVp.weight = 0f

        videoViewContainer.layoutParams = lpVideo
        flFragmentContainer.layoutParams = lpVp
        llVideoActContainer.post {
            llVideoActContainer.requestLayout()
        }
    }

    var lastBackTime = 0L
    override fun onBackPressed() {

        if (RoomManager.mCurrentRoom?.isRoomHost() == true) {
            CommonTipDialog.TipBuild()
                .setTittle("确定离开房间吗?")
                .setContent("房主离开后，房间自动解散，确定离开吗？")
                .setListener(object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        finish()
                    }
                }).buildNiuNiu().show(supportFragmentManager, "")
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastBackTime > 1500) {
            "再按一次退出".asToast()
            lastBackTime = now
        } else {
            super.onBackPressed()
        }
    }
}