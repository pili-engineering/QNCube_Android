package com.niucube.module.ktv

import com.niucube.module.ktv.mode.Song
import com.niucube.module.ktv.mode.kTVTemp
import com.qiniu.bzcomp.network.HttpListData
import com.qiniudemo.baseapp.been.RoomListItem
import retrofit2.http.*

interface KTVService {

    @Headers("headerForm2Json: true")
    @POST("/v1/ktv/songList")
    @FormUrlEncoded
    suspend fun songList(
        @Field("pageSize") pageSize: Int,
        @Field("pageNum") pageNum: Int,
        @Field("roomId") roomId: String,
    ): HttpListData<Song>

    @Headers("headerForm2Json: true")
    @POST("/v1/ktv/selectedSongList")
    @FormUrlEncoded
    suspend fun selectedSongList(
        @Field("pageSize") pageSize: Int,
        @Field("pageNum") pageNum: Int,
        @Field("roomId") roomId: String,
    ): HttpListData<Song>

    @Headers("headerForm2Json: true")
    @POST("/v1/ktv/operateSong")
    @FormUrlEncoded
    suspend fun operateSong(
        @Field("operateType") operateType: String,
        @Field("songId") songId: String,
        @Field("roomId") roomId: String,
    ): Any



}