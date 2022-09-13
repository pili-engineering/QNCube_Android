package com.niucube.module.videowatch

import android.net.Uri
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.DialogFragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.hapi.happy_dialog.FinalDialogFragment
import com.hipi.vm.backGround
//import com.niucube.compui.beauty.SenseTimePluginManager
import com.niucube.module.videowatch.mode.Movie
import com.niucube.player.video.contronller.DefaultController
import com.pili.pldroid.player.AVOptions
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.been.CreateRoomEntity
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.RoomService
import com.qiniudemo.baseapp.widget.CommonInputDialogDark
import kotlinx.android.synthetic.main.activity_moview_home.*

@Route(path = RouterConstant.VideoRoom.VideoHome)
class MovieHomeActivity : BaseActivity() {

    @Autowired
    @JvmField
    var solutionType = ""
    var defaultType: String = "movie"

    @Autowired
    @JvmField
    var movie: Movie? = null


    override fun initViewData() {
        lifecycle.addObserver(videoPlayer)
        videoPlayer.setPlayerConfig(videoPlayer.getPlayerConfig().setAVOptions(AVOptions().apply {
            setInteger(AVOptions.KEY_FAST_OPEN, 1);
            setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 5);
            setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
            setString(AVOptions.KEY_CACHE_DIR, cacheDir.absolutePath)
            setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_AUTO)
            setInteger(AVOptions.KEY_START_POSITION, 0 * 1000)
        }))
        val controller = DefaultController(this)
        videoPlayer.addController(controller)
        controller.onBackIconClickListener = View.OnClickListener { finish() }
        movie?.let {
            controller.setTitle(it.name)
            videoPlayer.setUp(Uri.parse(it.playUrl))
            videoPlayer.startPlay()
        }
        if (TextUtils.isEmpty(solutionType)) {
            solutionType = defaultType
        }
        cardCreate.setOnClickListener {
//            CommonCreateRoomDialog().apply {
//                setDefaultListener(object : FinalDialogFragment.BaseDialogListener() {
//                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
//                        super.onDialogPositiveClick(dialog, any)
            backGround {
                showLoading(true)
                doWork {
                    val room = RetrofitManager.create(RoomService::class.java)
                        .createRoom(CreateRoomEntity().apply {
                            title = movie?.name ?: ""
                            type = solutionType
                        })
                    ARouter.getInstance().build(RouterConstant.VideoRoom.VideoRoom)
                        .withString("solutionType", solutionType)
                        .withString("roomId", room.roomInfo!!.roomId)
                        .withSerializable("movie", movie)
                        .navigation(this@MovieHomeActivity)
                    finish()
                }
                catchError {
                    it.printStackTrace()
                    it.message?.asToast()
                }
                onFinally {
                    showLoading(false)
                }
//                        }
//                    }
//                }).show(supportFragmentManager, "")
            }
        }

        cardJoin.setOnClickListener {
            CommonInputDialogDark.newInstance("请输入邀请码", "请输入邀请码").apply {
                setDefaultListener(object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        ARouter.getInstance().build(RouterConstant.VideoRoom.VideoRoom)
                            .withString("solutionType", solutionType)
                            .withString("joinInvitationCode", any.toString())
                            .navigation(this@MovieHomeActivity)
                        finish()
                    }
                }).show(supportFragmentManager, "")
            }
        }
        cardList.setOnClickListener {
            ARouter.getInstance().build(RouterConstant.VideoRoom.VideoRoomList)
                .withString("solutionType", solutionType)
                .navigation(this@MovieHomeActivity)
            finish()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_moview_home
    }

}