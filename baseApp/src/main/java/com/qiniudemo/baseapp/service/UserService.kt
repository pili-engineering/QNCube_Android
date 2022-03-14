package com.qiniudemo.baseapp.service

import com.qiniu.bzcomp.user.UserInfo
import retrofit2.http.*

interface UserService {

    @GET("/v1/accountInfo/{accountId}")
    suspend fun getUserInfo(@Path("accountId") accountId: String): UserInfo

    @FormUrlEncoded
    @POST("/v1/accountInfo/{accountId}")
    suspend fun editUserInfo(@Path("accountId") accountId: String,@Field("nickname")nickname:String ): Any

}