package com.qncube.liveuikit

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.qbcube.pkservice.QNPKService
import com.qncube.chatservice.QNChatRoomService
import com.qncube.danmakuservice.QNDanmakuService
import com.qncube.linkmicservice.QNLinkMicService
import com.qncube.liveroom_pullclient.QNLivePullClient
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.asToast
import com.qncube.liveroomcore.getCode
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.publicchatservice.QNPublicChatService
import com.qncube.roomservice.QNRoomService
import com.qncube.rtcexcepion.RtcException
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.activity.BaseFrameActivity
import com.qncube.uikitcore.ext.bg
import com.qncube.uikitcore.view.CommonPagerAdapter
import com.qncube.uikitcore.view.EmptyFragment
import kotlinx.android.synthetic.main.activity_room_pull.*
import kotlinx.android.synthetic.main.activity_room_pull.linkerCotiner
import kotlinx.android.synthetic.main.activity_room_pull.vpCover
import kotlinx.android.synthetic.main.activity_room_push.*
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

    override fun init() {
        mRoomId = intent.getStringExtra("roomId") ?: ""
        mRoomClient.pullPreview = player
        vpCover.visibility = View.INVISIBLE
        vpCover.adapter = CommonPagerAdapter(fragments, supportFragmentManager)
        vpCover.currentItem = 1

        linkerCotiner.attach(
            QNLiveRoomUIKit.mViewSlotTable.mLinkerSlot,
            this, mKitContext, mRoomClient
        )
        QNLiveRoomUIKit.mViewSlotTable.mAnchorReceivedPKApplySlot.createView(
            this,
            mKitContext,
            mRoomClient,
            null
        )
        QNLiveRoomUIKit.mViewSlotTable.mAnchorReceivedLinkMicApplySlot.createView(
            this,
            mKitContext,
            mRoomClient,
            null
        )

        bg {
            showLoading(true)
            doWork {
                val room = suspendJoinRoom(mRoomId)
                //  c?.onSuccess(null)
                prevContainer.visibility = View.GONE
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

    override fun getLayoutId(): Int {
        return R.layout.activity_room_pull
    }

}