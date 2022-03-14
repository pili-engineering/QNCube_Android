package com.qizhou.bzupdate

import com.qiniu.bzcomp.network.HttpListData
import com.qiniu.bzcomp.network.HttpResp
import retrofit2.http.*

interface UpDataService {

    @GET("/v2/app/updates")
    suspend fun updates(
        @Query("version") version: String,
        @Query("arch") arch: String,
    ): UpDataModel
}