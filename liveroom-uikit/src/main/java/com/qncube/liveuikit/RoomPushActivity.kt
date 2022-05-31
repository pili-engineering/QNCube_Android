package com.qncube.liveuikit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.nucube.rtclive.QNCameraParams
import com.nucube.rtclive.QNMicrophoneParams
import com.qbcube.pkservice.QNPKService
import com.qncube.chatservice.QNChatRoomService
import com.qncube.danmakuservice.QNDanmakuService
import com.qncube.linkmicservice.QNLinkMicService
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser
import com.qncube.publicchatservice.QNPublicChatService
import com.qncube.pushclient.QNLivePushClient
import com.qncube.roomservice.QNRoomService
import com.qncube.rtcexcepion.RtcException
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.activity.BaseFrameActivity
import com.qncube.uikitcore.ext.bg
import com.qncube.uikitcore.ext.permission.PermissionAnywhere
import com.qncube.uikitcore.view.CommonPagerAdapter
import com.qncube.uikitcore.view.EmptyFragment
import kotlinx.android.synthetic.main.activity_room_push.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoomPushActivity : BaseFrameActivity() {

    companion object {
        private var startCallBack: QNLiveCallBack<QNLiveRoomInfo>? = null

        fun start(context: Context, callBack: QNLiveCallBack<QNLiveRoomInfo>) {
            startCallBack = callBack
            val i = Intent(context, RoomPushActivity::class.java)
            context.startActivity(i)

        }
    }

    private val mRoomClient by lazy {
        QNLivePushClient.createLivePushClient().apply {
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
            override var androidContext: Context = this@RoomPushActivity
            override var fm: FragmentManager = supportFragmentManager
            override var currentActivity: FragmentActivity = this@RoomPushActivity
        }
    }

    private val mQNRoomLifeCycleListener = object : QNRoomLifeCycleListener {
        override fun onRoomEnter(roomId: String, user: QNLiveUser) {}

        override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
            startCallBack?.onSuccess(roomInfo)
            startCallBack = null
            prevContainer.visibility = View.GONE
            vpCover.visibility = View.VISIBLE
        }

        override fun onRoomLeave() {}

        override fun onRoomClose() {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//设置透明导航栏
        }
        super.onCreate(savedInstanceState)
    }
    private fun start() {
        flPkContainer.attach(
            QNLiveRoomUIKit.mViewSlotTable.mPKAnchorPreviewSlot,
            this, mKitContext, mRoomClient
        )

        prevContainer.attach(
            QNLiveRoomUIKit.mViewSlotTable.mLivePreViewSlot,
            this, mKitContext, mRoomClient
        )

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

        mRoomClient.enableCamera(QNCameraParams())
        mRoomClient.enableMicrophone(QNMicrophoneParams())
        mRoomClient.localPreView = preTextureView
        vpCover.visibility = View.INVISIBLE
        vpCover.adapter = CommonPagerAdapter(fragments, supportFragmentManager)
        vpCover.currentItem = 1
        mRoomClient.addRoomLifeCycleListener(mQNRoomLifeCycleListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRoomClient.closeRoom()
        startCallBack?.onError(-1, "")
        startCallBack = null
    }

    override fun init() {

        PermissionAnywhere.requestPermission(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        ) { grantedPermissions, _, _ ->
            if (grantedPermissions.size == 2) {
                start()
            } else {
                "请同意必要的权限".asToast()
                startCallBack?.onError(-1, "no permission")
                startCallBack = null
                finish()
            }
        }
    }
    //安卓重写返回键事件
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK
            && mRoomClient.getService(QNRoomService::class.java).currentRoomInfo!=null
        ) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun isToolBarEnable(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_room_push
    }

}