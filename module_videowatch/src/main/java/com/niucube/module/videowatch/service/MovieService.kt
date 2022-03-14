package com.niucube.module.videowatch.service

import com.niucube.module.videowatch.mode.Movie
import com.qiniu.bzcomp.network.HttpListData
import retrofit2.http.*

interface MovieService {

   // @Headers("headerForm2Json: true")
    @GET("/v1/watchMoviesTogether/movieList")
    suspend fun movieList(
       @Query("pageSize") pageSize: Int,
       @Query("pageNum") pageNum: Int,
       @Query("roomId") roomId: String,
    ): HttpListData<Movie>


    @GET("/v1/watchMoviesTogether/selectedMovieList")
    suspend fun selectedMovieList(
        @Query("pageSize") pageSize: Int,
        @Query("pageNum") pageNum: Int,
        @Query("roomId") roomId: String,
    ): HttpListData<Movie>

    @Headers("headerForm2Json: true")
    @POST("/v1/watchMoviesTogether/movieOperation")
    @FormUrlEncoded
    suspend fun movieOperation(
        @Field("operateType") operateType: String,
        @Field("movieId") songId: String,
        @Field("roomId") roomId: String,
    ): Any

    @Headers("headerForm2Json: true")
    @POST("/v1/watchMoviesTogether/switchMovie")
    @FormUrlEncoded
    suspend fun switchMovie(
        @Field("movieId") songId: String,
        @Field("roomId") roomId: String,
    ): Any

}