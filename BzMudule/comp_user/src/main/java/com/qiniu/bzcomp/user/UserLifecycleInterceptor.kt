package com.qiniu.bzcomp.user

interface UserLifecycleInterceptor {

    fun onLogout(toastStr: String = "")
    suspend fun onLogin(loginToken: LoginToken)
    fun onUserInfoRefresh(userInfo: UserInfo)

}