package com.nucube.module.lowcodeliving

import retrofit2.http.GET
import retrofit2.http.Query

interface LiveSdkService {

    @GET("/v1/live/auth_token")
    suspend fun getTokenInfo(
        @Query("userID") userID: String,
        @Query("deviceID") deviceID: String
    ): LowCodeSdkToken

}