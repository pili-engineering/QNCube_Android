package com.qncube.liveuikit

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.pili.pldroid.player.PLOnVideoSizeChangedListener
import com.pili.pldroid.player.widget.PLVideoView
import com.qbcube.pkservice.QNPKService
import com.qncube.chatservice.QNChatRoomService
import com.qncube.chatservice.QNChatRoomServiceListener
import com.qncube.danmakuservice.QNDanmakuService
import com.qncube.linkmicservice.QNLinkMicService
import com.qncube.liveroom_pullclient.QNLivePullClient
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.publicchatservice.QNPublicChatService
import com.qncube.roomservice.QNRoomService
import com.qncube.rtcexcepion.RtcException
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.activity.BaseFrameActivity
import com.qncube.uikitcore.dialog.CommonTipDialog
import com.qncube.uikitcore.ext.bg
import com.qncube.uikitcore.view.CommonPagerAdapter
import com.qncube.uikitcore.view.EmptyFragment
import kotlinx.android.synthetic.main.activity_room_pull.*
import kotlinx.android.synthetic.main.activity_room_pull.bgImgContainer
import kotlinx.android.synthetic.main.activity_room_pull.flPkContainer
import kotlinx.android.synthetic.main.activity_room_pull.linkerCotiner
import kotlinx.android.synthetic.main.activity_room_pull.vpCover
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoomPullActivity : BaseFrameActivity() {

    companion object {
        private var startCallBack: QNLiveCallBack<QNLiveRoomInfo>? = null
        fun start(context: Context, roomId: String, callBack: QNLiveCallBack<QNLiveRoomInfo>?) {
            startCallBack = callBack
            val i = Intent(context, RoomPullActivity::class.java)
            i.putExtra("roomId", roomId)
            context.startActivity(i)
        }
    }

    private var mRoomId = ""
    private val mRoomClient by lazy {
        QNLivePullClient.createLivePullClient().apply {
            registerService(
                QNChatRoomService::class.java,
            )
            registerService(
                QNPKService::class.java,
            )
            registerService(
                QNLinkMicService::class.java,
            )
            registerService(
                QNDanmakuService::class.java,
            )
            registerService(
                QNPublicChatService::class.java,
            )
            registerService(
                QNRoomService::class.java
            )
            setPullClientListener { liveRoomStatus, msg ->
                if (liveRoomStatus == LiveStatus.LiveStatusOff.intValue) {
                    LiveStatus.LiveStatusOff.tipMsg.asToast()
                    finish()
                }
                QNLiveLogUtil.LogE("房间状态变更  ${liveRoomStatus}")
            }
        }
    }

    private val fragments by lazy {
        listOf(EmptyFragment(), CoverFragment().apply {
            mClient = mRoomClient
        })
    }

    private val mKitContext by lazy {
        object : KitContext {
            override var androidContext: Context = this@RoomPullActivity
            override var fm: FragmentManager = supportFragmentManager
            override var currentActivity: FragmentActivity = this@RoomPullActivity
        }
    }


    private suspend fun suspendJoinRoom(roomId: String) = suspendCoroutine<QNLiveRoomInfo> { cont ->
        mRoomClient.joinRoom(roomId, object : QNLiveCallBack<QNLiveRoomInfo> {
            override fun onError(code: Int, msg: String) {
                cont.resumeWithException(RtcException(code, msg))
            }

            override fun onSuccess(data: QNLiveRoomInfo) {
                cont.resume(data)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//设置透明导航栏
        }
        super.onCreate(savedInstanceState)
    }

    override fun init() {
        mRoomId = intent.getStringExtra("roomId") ?: ""
        mRoomClient.pullPreview = player
        vpCover.visibility = View.INVISIBLE
        vpCover.adapter = CommonPagerAdapter(fragments, supportFragmentManager)
        vpCover.currentItem = 1

        bgImgContainer.attach(
            QNLiveRoomUIKit.mViewSlotTable.mRoomBackGroundSlot,
            this, mKitContext, mRoomClient
        )

        linkerCotiner.attach(
            QNLiveRoomUIKit.mViewSlotTable.mLinkerSlot,
            this, mKitContext, mRoomClient
        )

        flPkContainer.attach(
            QNLiveRoomUIKit.mViewSlotTable.mPKAnchorPreviewSlot,
            this, mKitContext, mRoomClient
        )

        vpCover.post {
            bg {
                showLoading(true)
                doWork {
                    val room = suspendJoinRoom(mRoomId)
                    //  c?.onSuccess(null)

                    vpCover.visibility = View.VISIBLE
                    startCallBack?.onSuccess(room)
                }
                catchError {
                    it.message?.asToast()
                    startCallBack?.onError(it.getCode(), it.message)
                    finish()
                    //  c?.onError(it.getCode(), it.message)
                }

                onFinally {
                    startCallBack = null
                    showLoading(false)
                }
            }
        }

        player.displayAspectRatio = PLVideoView.ASPECT_RATIO_PAVED_PARENT

        player.setOnVideoSizeChangedListener { w, h ->
            Log.d("player", "  setOnVideoSizeChangedListener ${w} ${h}");
        }

    }

    //安卓重写返回键事件
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK
            && mRoomClient.getService(QNRoomService::class.java).currentRoomInfo != null
        ) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRoomClient.closeRoom()
        startCallBack = null
    }

    override fun isToolBarEnable(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_room_pull
    }

}