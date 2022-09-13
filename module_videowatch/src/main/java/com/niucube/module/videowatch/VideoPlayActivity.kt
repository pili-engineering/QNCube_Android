package com.niucube.module.videowatch

import android.net.Uri
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.hipi.vm.backGround
import com.niucube.player.video.contronller.DefaultController
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.RoomService
import kotlinx.android.synthetic.main.activity_video_play.*


@Route(path = RouterConstant.VideoRoom.VideoPlayer)
class VideoPlayActivity : BaseActivity() {

    @Autowired
    @JvmField
    var solutionType = ""

    @Autowired
    @JvmField
    var roomId = ""


    override fun initViewData() {
        lifecycle.addObserver(videoPlayer)
        val controller = DefaultController(this)
        controller.setSeekAble(false)
        controller.setSeekVisibility(View.INVISIBLE)
        controller.onBackIconClickListener =  View.OnClickListener { finish() }
        videoPlayer.addController(controller)
        backGround {
            showLoading(true)
            doWork {
                val info = RetrofitManager.create(RoomService::class.java).getRoomInfo(
                    solutionType,
                    roomId
                )
                videoPlayer.setUp(Uri.parse(info.providePushUri()))
                videoPlayer.startPlay()
            }
            catchError {
                it.printStackTrace()
                it.message?.asToast()
            }
            onFinally {
                showLoading(false)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_video_play
    }
}