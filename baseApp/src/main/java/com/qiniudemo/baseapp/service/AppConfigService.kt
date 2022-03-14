package com.qiniudemo.baseapp.service

import com.qiniudemo.baseapp.been.AppConfigModel
import com.qiniu.bzcomp.network.HttpListData
import com.qiniu.bzcomp.user.LoginToken
import com.qiniudemo.baseapp.been.QiniuApp
import com.qiniudemo.baseapp.been.TokenData
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface AppConfigService {

    @GET("/v1/appConfig")
    suspend fun appConfig(): AppConfigModel

    @GET("/v1/solution")
    suspend fun solutions(): HttpListData<QiniuApp>


    @FormUrlEncoded
    @POST("/v1/token/getToken")
    fun getToken(@Field("content") content: String): Call<TokenData>

}