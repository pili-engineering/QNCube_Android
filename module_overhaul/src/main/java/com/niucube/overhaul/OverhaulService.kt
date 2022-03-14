package com.niucube.overhaul

import com.niucube.overhaul.mode.HeartBeat
import com.niucube.overhaul.mode.OverhaulRoom
import com.niucube.overhaul.mode.OverhaulRoomItem
import com.qiniu.bzcomp.network.HttpListData
import retrofit2.http.*

interface OverhaulService {

    /**      * 面试列表      */
    @GET("/v1/repair/listRoom")
    suspend fun overhaulList(
        @Query("pageSize") pageSize: Int,
        @Query("pageNum") pageNum: Int
    ): HttpListData<OverhaulRoomItem>

    @FormUrlEncoded
    @POST("/v1/repair/createRoom")
    suspend fun createInterview(
        @Field("title") title: String,
        @Field("role") role: String
    ): OverhaulRoom


    @FormUrlEncoded
    @POST("/v1/repair/joinRoom")
    suspend fun joinRoom(
        @Field("roomId") roomId: String,
        @Field("role") role: String
    ): OverhaulRoom


    @GET("/v1/repair/leaveRoom/{roomID}")
    suspend fun leaveRoom(@Path("roomID") roomID: String)

    @GET("/v1/repair/heartBeat/{roomId}")
    suspend fun heartBeat(
        @Path("roomId") roomId: String
    ): HeartBeat

    @GET("/v1/repair/getRoomInfo/{roomId}")
    suspend fun getRoomInfo(@Path("roomId") roomId: String):OverhaulRoom


}