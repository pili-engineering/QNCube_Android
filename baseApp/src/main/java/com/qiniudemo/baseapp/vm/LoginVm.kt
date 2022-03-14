package com.qiniudemo.baseapp.vm

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.hipi.vm.*
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniudemo.baseapp.service.LoginService
import com.qiniudemo.baseapp.service.UserService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginVm(application: Application, bundle: Bundle?) : BaseViewModel(application, bundle) {

    fun getVerificationCode(phone_number: String, call: LifecycleUiCall<Unit>) {
        vmScopeBg {
            doWork {
                showLoadingCall?.invoke(true)
                RetrofitManager.create(LoginService::class.java)
                    .sendSmsCode(phone_number)
                call.onNext(Unit)
            }
            onFinally {
                showLoadingCall?.invoke(false)
            }
        }
    }

    fun login(call: LifecycleUiCall<Boolean>) {
        if (UserInfoManager.getUserToken().isEmpty() || (UserInfoManager.getUserInfo()?.phone
                ?: "").isEmpty()
        ) {
            call.onNext(false)
            return
        }
        val handler = CoroutineExceptionHandler { _, e ->
            toast(e.message)
            e.printStackTrace()
            showLoadingCall?.invoke(false)
            call.onNext(false)
        }

        viewModelScope.launch(Dispatchers.Main + handler) {
            val uinfo = RetrofitManager.create(LoginService::class.java).signInWithToken(
                UserInfoManager.getUserInfo()?.phone ?: ""
            )
            //保存用户信息
            UserInfoManager.updateLoginModel(uinfo)
            //保存用户信息
            val info = RetrofitManager.create(UserService::class.java)
                .getUserInfo(UserInfoManager.getUserId())
            UserInfoManager.updateUserInfo(info)
            call.onNext(true)
            showLoadingCall?.invoke(false)
        }
    }

    fun login(phoneNumber: String, smsCode: String, call: LifecycleUiCall<Boolean>) {
        val handler = CoroutineExceptionHandler { _, e ->
            toast(e.message)
            e.printStackTrace()
            showLoadingCall?.invoke(false)
            call.onNext(false)
        }
        showLoadingCall?.invoke(true)
        viewModelScope.launch(Dispatchers.Main + handler) {
            val uinfo = RetrofitManager.create(LoginService::class.java).login(
                phoneNumber, smsCode
            )
            //保存用户信息
            UserInfoManager.updateLoginModel(uinfo)
            //保存用户信息
            val info = RetrofitManager.create(UserService::class.java)
                .getUserInfo(UserInfoManager.getUserId())
            UserInfoManager.updateUserInfo(info)
            call.onNext(true)
            showLoadingCall?.invoke(false)
        }
    }
}