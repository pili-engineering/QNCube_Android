package com.qiniu.bzcomp.user

import android.text.TextUtils
import android.widget.Toast
import com.hapi.ut.AppCache
import com.hapi.ut.SpUtil
import com.qiniu.jsonutil.JsonUtils
import com.qiniusdk.userinfoprovide.UserInfoProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object UserInfoManager {

    private var uid = ""
    private var mUserInfo: UserInfo? = null
    var mLoginToken: LoginToken? = null
    private set

    fun init() {
        mUserInfo = JsonUtils.parseObject(
            SpUtil.get(SpName)?.readString(
                KEY_USER_INFO
            ) ?: "",
            UserInfo::class.java
        ) as UserInfo?
        uid = mUserInfo?.accountId ?: ""

        mLoginToken = JsonUtils.parseObject(
            SpUtil.get(SpName)?.readString(
                KEY_USER_LOGIN_MODEL
            ) ?: "",
            LoginToken::class.java
        ) as LoginToken?
        mLoginToken?.let {
            uid = it.accountId
        }
        UserInfoProvider.getLoginUserAvatarCall = {
            mUserInfo?.avatar ?: ""
        }
        UserInfoProvider.getLoginUserIdCall = {
            mUserInfo?.accountId ?: ""
        }
        UserInfoProvider.getLoginUserNameCall = {
            mUserInfo?.nickname ?: ""
        }
    }

    fun getUserInfo(): UserInfo? {
        return mUserInfo
    }

    /**
     * 快捷获取　uid
     */
    fun getUserId(): String {
        return uid
    }

    /**
     * 快捷获取　token
     */
    fun getUserToken(): String {
        return mLoginToken?.loginToken ?: ""
    }

    fun updateUserInfo(userInfo: UserInfo) {
        uid = userInfo.accountId
        mUserInfo = userInfo
        saveUserInfoToSp()
        UserLifecycleManager.chain {
            it.onUserInfoRefresh(userInfo)
        }
    }

    suspend fun updateLoginModel(loginToken: LoginToken) {
        uid = loginToken.accountId
        mLoginToken = loginToken
        saveLoginInfoToSp()
        UserLifecycleManager.userLifecycleInterceptors.forEach {
            it.onLogin(loginToken)
        }
    }

    //存sp
    private fun saveUserInfoToSp() {
        mUserInfo?.let {
            SpUtil.get(SpName)
                .saveData(KEY_USER_INFO, JsonUtils.toJson(it))
        }
    }

    private fun saveLoginInfoToSp() {
        mLoginToken?.let {
            SpUtil.get(SpName)
                .saveData(KEY_USER_LOGIN_MODEL, JsonUtils.toJson(it))
        }
    }

    fun hasLogin(): Boolean {
        return !getUserId().isEmpty() && !TextUtils.isEmpty(
            getUserToken()
        )
    }

    fun onLogout(toastStr: String = "") {

        GlobalScope.launch(Dispatchers.Main) {
            if (!toastStr.isEmpty()) {
                Toast.makeText(AppCache.getContext(),toastStr,Toast.LENGTH_SHORT).show()
            }
            clearUser()
            UserLifecycleManager.chain {
                it.onLogout(toastStr)
            }
        }
    }

    fun clearUser() {
        SpUtil.get(SpName).clear()
        uid = ""
        mUserInfo = null
        mLoginToken = null
    }

    private var SpName = "config:user"
    private val KEY_UID = "uid"
    private val KEY_USER_INFO = "user_info"
    private val KEY_USER_LOGIN_MODEL = "USER_LOGIN_MODEL"
}