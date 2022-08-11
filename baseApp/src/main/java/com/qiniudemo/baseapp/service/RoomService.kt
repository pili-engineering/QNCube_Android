package com.qiniudemo.baseapp.service

import com.qiniu.bzcomp.network.HttpListData
import com.qiniu.bzcomp.network.HttpRoomsData
import com.qiniudemo.baseapp.been.*
import retrofit2.http.*

interface RoomService {

    @POST("/v1/base/createRoom")
    suspend fun createRoom(@Body createRoomEntity: CreateRoomEntity): BaseRoomEntity

    @POST("/v1/base/joinRoom")
    suspend  fun  joinRoom(
        @Body joinRoomEntity: JoinRoomEntity
    ): BaseRoomEntity

    @POST("/v1/base/leaveRoom")
    suspend fun leaveRoom(
       @Body room:RoomIdType
    )

    @GET("/v1/base/listRoom")
    suspend fun listRoom(
        @Query("pageSize") pageSize: Int,
        @Query("pageNum") pageNum: Int,
        @Query("type") type: String,
    ): HttpListData<RoomListItem>

    @GET("/v1/base/heartBeat")
    suspend fun heartBeat(
        @Query("type") type: String,
        @Query("roomId") roomId: String
    ): HeartBeat

    @GET("/v1/base/getRoomInfo")
    suspend fun getRoomInfo(
        @Query("type") type: String,
        @Query("roomId") roomId: String
    ): BaseRoomEntity

}