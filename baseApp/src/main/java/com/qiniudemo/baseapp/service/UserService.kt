package com.qiniudemo.baseapp.service

import com.qiniu.bzcomp.user.UserInfo
import com.qiniudemo.baseapp.been.FileUploadResp
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface UserService {

    @GET("/v1/accountInfo/{accountId}")
    suspend fun getUserInfo(@Path("accountId") accountId: String): UserInfo

    @FormUrlEncoded
    @POST("/v1/accountInfo/{accountId}")
    suspend fun editUserInfo(
        @Path("accountId") accountId: String,
        @Field("nickname") nickname: String
    ): Any

    @FormUrlEncoded
    @POST("/v1/accountInfo/{accountId}")
    suspend fun editUserAvatar(
        @Path("accountId") accountId: String,
        @Field("avatar") avatar: String
    ): Any

    @Multipart
    @POST("/v1/upload")
    suspend fun upload(@Part fileBody:  MultipartBody.Part): FileUploadResp
}