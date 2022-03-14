package com.qiniudemo.module.interview

import com.qiniu.bzcomp.network.HttpListData
import com.qiniudemo.module.interview.been.HeartBeat
import com.qiniudemo.module.interview.been.InterViewDetails
import com.qiniudemo.module.interview.been.InterViewInfo
import com.qiniudemo.module.interview.been.InterviewRoomModel
import retrofit2.http.*

interface InterviewService {
    /**      * 面试列表      */
    @GET("/v1/interview")
    suspend fun interviewList(
        @Query("pageSize") pageSize: Int,
        @Query("pageNum") pageNum: Int
    ): HttpListData<InterViewInfo>

    @GET("/v1/interview/{interviewId}")
    suspend fun interviewDetails(@Path("interviewId") interviewId: String): InterViewDetails

    @FormUrlEncoded
    @POST("/v1/interview")
    suspend fun createInterview(
        @Field("title") title: String,
        @Field("startTime") startTime: String,
        @Field("endTime") endTime: String,
        @Field("goverment") goverment: String,
        @Field("career") career: String,
        @Field("isAuth") isAuth: String,
        @Field("authCode") authCode: String,
        @Field("isRecorded") isRecorded: String,
        @Field("candidateName") candidateName: String,
        @Field("candidatePhone") candidatePhone: String
    ): Any

    @FormUrlEncoded
    @POST("/v1/interview/{interviewId}")
    suspend fun modifyInterview(
        @Path("interviewId") interviewId: String,
        @Field("title") title: String,
        @Field("startTime") startTime: String,
        @Field("endTime") endTime: String,
        @Field("goverment") goverment: String,
        @Field("career") career: String,
        @Field("isAuth") isAuth: String,
        @Field("authCode") authCode: String,
        @Field("isRecorded") isRecorded: String,
        @Field("candidateName") candidateName: String,
        @Field("candidatePhone") candidatePhone: String
    ): Any

    @POST("/v1/endInterview/{interviewId}")
    suspend fun endInterview(
        @Path("interviewId") interviewId: String
    ): Any

    @POST("/v1/leaveInterview/{interviewId}")
    suspend fun leavelInterview(
        @Path("interviewId") interviewId: String
    ): Any

    @POST("/v1/joinInterview/{interviewId}")
    suspend fun joinInterview(
        @Path("interviewId") interviewId: String
    ): InterviewRoomModel

    @GET("/v1/heartBeat/{interviewId}")
    suspend fun heartBeat(
        @Path("interviewId") interviewId: String
    ): HeartBeat
}