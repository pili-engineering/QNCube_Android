package com.nucube.module.lowcodeliving

import android.view.View
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.hipi.vm.backGround
import com.nucube.module.lowcodeliving.databinding.ActivityPkliveRoomListBinding
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.qnim.QNIMManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.ext.asToast
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.QLiveConfig
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.sdk.QLive
import com.qlive.sdk.QUserInfo
import com.qlive.shoppingservice.QItem
import com.qlive.uikit.RoomPage
import com.qlive.uikit.component.CloseRoomView
import com.qlive.uikit.component.LiveRecordListView
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitshopping.PlayerShoppingDialog
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Route(path = RouterConstant.LowCodePKLive.LiveRoomList)
class PKLiveRoomListActivity : BaseActivity<ActivityPkliveRoomListBinding>() {

    companion object {
        init {
            //自定义事件
            PlayerShoppingDialog.onItemClickCall =
                { context: QLiveUIKitContext, client: QLiveClient, view: View, item: QItem ->
                    TestShoppingActivity.start(context, item)
                }
            CloseRoomView.beforeFinishCall = { context: QLiveUIKitContext,
                                               client: QLiveClient,
                                               room: QLiveRoomInfo,
                                               isAnchorActionCloseRoom: Boolean ->
                if (isAnchorActionCloseRoom) {
                    DemoLiveFinishedActivity.checkStart(context.androidContext, room)
                }
            }
            LiveRecordListView.onClickFinishedRoomCall = { context, roomInfo ->
                DemoLiveFinishedActivity.checkStart(context, roomInfo)
            }
        }
    }

    @Autowired
    @JvmField
    var layoutType = 1

    /**
     * 初始化sdk
     */
    suspend fun suspendInit() =
        suspendCoroutine<Unit> { coroutine ->
            QLive.init(
                application, QLiveConfig()
            ) { callback ->
                //业务方获取token
                backGround {
                    doWork {
                        val token = RetrofitManager.create(LiveSdkService::class.java)
                            .getTokenInfo(
                                UserInfoManager.getUserId(),
                                Math.random().toString() + ""
                            )
                        callback.onSuccess(token.accessToken)
                    }
                    catchError {
                        it.printStackTrace()
                    }
                }
            }

            when (layoutType) {
                1 -> {
                    QLive.getLiveUIKit().getPage(RoomPage::class.java).playerCustomLayoutID =
                        R.layout.activity_room_player
                    QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID =
                        R.layout.activity_room_pusher
                }
                2 -> {
                    QLive.getLiveUIKit().getPage(RoomPage::class.java).playerCustomLayoutID =
                        R.layout.custom_layout_player_noshopping
                    QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID =
                        R.layout.custom_layout_pusher_noshoping
                }
                3 -> {
                    QLive.getLiveUIKit().getPage(RoomPage::class.java).playerCustomLayoutID =
                        R.layout.ktv_activity_room_player
                    QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID =
                        R.layout.ktv_activity_room_pusher
                }

            }

            QLive.auth(object : QLiveCallBack<Void> {
                override fun onError(p0: Int, p1: String?) {
                    coroutine.resumeWithException(Exception("$p1 "))
                }

                override fun onSuccess(p0: Void?) {
                    coroutine.resume(Unit)
                }
            })
        }

    /**
     *  //绑定用户信息 绑定后房间在线用户能返回绑定设置的字段
     */
    suspend fun suspendSetUser() =
        suspendCoroutine<Unit> { coroutine ->
            //绑定用户信息 绑定后房间在线用户能返回绑定设置的字段
            QLive.setUser(QUserInfo().apply {
                avatar = UserInfoManager.getUserInfo()?.avatar ?: ""
                nick = UserInfoManager.getUserInfo()?.nickname ?: ""
                extension = HashMap<String, String>().apply {
                    put("phone", "13141616037")
                    put("customFiled", "i am customFile")
                }
            }, object : QLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    Toast.makeText(this@PKLiveRoomListActivity, msg, Toast.LENGTH_SHORT).show()
                    coroutine.resumeWithException(Exception("getTokenError"))
                }

                override fun onSuccess(data: Void?) {
                    coroutine.resume(Unit)
                }
            })
        }

    override fun init() {
        showLoading(true)
        backGround {
            doWork {
                //低代码im和牛魔方冲突
                QNIMManager.mRtmAdapter.suspendLoginOut()
                QNIMManager.unInit()
                suspendInit()
                suspendSetUser()
                QLive.getLiveUIKit().launch(this@PKLiveRoomListActivity)
            }
            catchError {
                it.message?.asToast()
                it.printStackTrace()
            }
            onFinally {
                showLoading(false)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun isToolBarEnable(): Boolean {
        return false
    }

    override fun isTitleCenter(): Boolean {
        return true
    }

    override fun isTranslucentBar(): Boolean {
        return false
    }

}