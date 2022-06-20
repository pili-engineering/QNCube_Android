package com.nucube.module.lowcodeliving

import android.content.Context

import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
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

    }

    suspend fun suspendInit(context: Context, token: String) =
        suspendCoroutine<Unit> { ct ->

        }

//    suspend fun suspendUpdateUserInfo(
//    ) = suspendCoroutine<QNLiveUser>
//    { ct ->
//        QNLiveRoomEngine.updateUserInfo(
//            UserInfoManager.getUserInfo()?.avatar,
//            UserInfoManager.getUserInfo()?.nickname,
//            HashMap<String, String>().apply {
//                         put("vip","1") //自定义vip等级
//                         put("level","22")
//            },
//            object : QNLiveCallBack<QNLiveUser> {
//                override fun onError(code: Int, msg: String?) {
//                    ct.resumeWithException(Exception(msg ?: ""))
//                }
//
//                override fun onSuccess(data: QNLiveUser) {
//                    ct.resume(data)
//                }
//            })
//    }

    override fun initViewData() {
        setToolbarTitle("主播列表")
//        if (!isInit) {
//            bg {
//                doWork {
//
//                    val token = RetrofitManager.create(LiveSdkService::class.java)
//                        .getRoomMicInfo(UserInfoManager.getUserId(), UserInfoManager.getUserId())
//                    suspendInit(applicationContext, token.accessToken)
//                    suspendUpdateUserInfo()
//                    isInit = true
//                    start()
//                }
//                catchError {
//                    if(it.message?.isEmpty()==true){
//                        Toast.makeText(this@PKLiveRoomListActivity,it.message,Toast.LENGTH_SHORT).show()
//                    }
//                   // it.message?.asToast()
//                    finish()
//                }
//            }
//        } else {
//            start()
//        }
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