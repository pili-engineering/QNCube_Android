package com.qiniu.bzcomp.network;

import com.qiniu.bzcomp.user.UserInfoManager
import okhttp3.*
import java.io.IOException


class QiniuRequestInterceptor : Interceptor {

    companion object{
        var appVersionName = ""
    }
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            //   this.header("Content-Type", "application/x-www-form-urlencoded")
            if (!UserInfoManager.getUserToken().isEmpty()) {
                this.header("Authorization", "Bearer " + UserInfoManager.getUserToken())
            }
            this.header("Q-Plat", "android")
            this.header("Q-Ver", appVersionName)
        }
            .build()
        //添加公共参数
        val response = chain.proceed(request)
        return response
    }
}



