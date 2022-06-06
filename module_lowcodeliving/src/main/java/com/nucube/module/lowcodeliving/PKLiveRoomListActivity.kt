package com.nucube.module.lowcodeliving

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.QNLiveRoomEngine
import com.qncube.liveroomcore.asToast
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser
import com.qncube.liveuikit.QNLiveRoomUIKit
import com.qncube.uikitcore.ext.bg
import kotlinx.android.synthetic.main.activity_pklive_room_list.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Route(path = RouterConstant.LowCodePKLive.LiveRoomList)
class PKLiveRoomListActivity : BaseActivity() {

    companion object {
        private var isInit = false
    }

    private fun start() {
        roomListView.attach(this)
        tvCreateRoom.setOnClickListener {
            QNLiveRoomUIKit.createAndJoinRoom(this, object : QNLiveCallBack<QNLiveRoomInfo> {
                override fun onError(code: Int, msg: String?) {
                    msg?.asToast()
                }

                override fun onSuccess(data: QNLiveRoomInfo?) {}
            })
        }
    }

    suspend fun suspendInit(context: Context, token: String) =
        suspendCoroutine<Unit> { ct ->
            QNLiveRoomEngine.init(context, token, object : QNLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    ct.resumeWithException(Exception(msg ?: ""))
                }

                override fun onSuccess(data: Void?) {
                    ct.resume(Unit)
                }
            })
        }

    suspend fun suspendUpdateUserInfo(
    ) = suspendCoroutine<QNLiveUser>
    { ct ->
        QNLiveRoomEngine.updateUserInfo(
            UserInfoManager.getUserInfo()?.avatar,
            UserInfoManager.getUserInfo()?.nickname,
            null,
            object : QNLiveCallBack<QNLiveUser> {
                override fun onError(code: Int, msg: String?) {
                    ct.resumeWithException(Exception(msg ?: ""))
                }

                override fun onSuccess(data: QNLiveUser) {
                    ct.resume(data)
                }
            })
    }

    override fun initViewData() {
        setToolbarTitle("主播列表")
        if (!isInit) {
            bg {
                doWork {

                    val token = RetrofitManager.create(LiveSdkService::class.java)
                        .getRoomMicInfo(UserInfoManager.getUserId(), UserInfoManager.getUserId())
                    suspendInit(applicationContext, token.accessToken)
                    suspendUpdateUserInfo()
                    isInit = true
                    start()
                }
                catchError {
                    if(it.message?.isEmpty()==true){
                        Toast.makeText(this@PKLiveRoomListActivity,it.message,Toast.LENGTH_SHORT).show()
                    }
                   // it.message?.asToast()
                    finish()
                }
            }
        } else {
            start()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_pklive_room_list
    }

    override fun isToolBarEnable(): Boolean {
        return true
    }

    override fun isTitleCenter(): Boolean {
        return true
    }

    override fun isTranslucentBar(): Boolean {
        return false
    }

}