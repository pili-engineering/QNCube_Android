package com.nucube.module.lowcodeliving

import okhttp3.RequestBody
import retrofit2.http.*

interface LiveSdkService {

    @GET("/v1/live/auth_token")
    suspend fun getTokenInfo(
        @Query("userID") userID: String,
        @Query("deviceID") deviceID: String
    ): LowCodeSdkToken

    @GET("/v1/live/IsRegister")
    suspend fun isRegister(
    ): Boolean


    @POST("/v1/live/statistics")
    suspend fun statistics(
        @Body  body: RequestBody,
    )
}