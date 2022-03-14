package com.niucube.channelattributes

import com.qiniudemo.baseapp.been.Attribute
import retrofit2.http.*

interface AttributeService {

    @POST("/v1/base/upMic")
    suspend fun upMic(@Body sitDownParams: SitDownParams)

    @Headers("headerForm2Json: true")
    @FormUrlEncoded
    @POST("/v1/base/downMic")
    suspend fun downMic(
        @Field("roomId") roomId: String,
        @Field("type") type: String,
        @Field("uid") uid: String,
        )

    @POST("/v1/base/updateRoomAttr")
    suspend fun updateRoomAttr(@Body updateRoomAttrParams: UpdateRoomAttrParams)

    @POST("/v1/base/updateMicAttr")
    suspend fun updateMicAttr(@Body updateMicAttrParams: UpdateMicAttrParams)

    @GET("/v1/base/getRoomMicInfo")
    suspend fun getRoomMicInfo(
        @Query("type") type: String,
        @Query("roomId") roomId: String
    ): AttrRoom


    @GET("/v1/base/getRoomAttr")
    suspend fun getRoomAttr(
        @Query("roomId") roomId: String,
        @Query("type") type: String,
        @Query("attrKey") attrKey: String,
    ):GetRoomAttrResp


    @GET("/v1/base/getMicAttr")
    suspend fun getMicAttr(
        @Query("roomId") roomId: String,
        @Query("type") type: String,
        @Query("uid") uid: String,
        @Query("attrKey") attrKey: String,
    ):GetRoomAttrResp

}


class GetRoomAttrResp {
    var attrs:List<Attribute>? =null
}
class UpdateRoomAttrParams {
    var roomId = ""
    var type = "";
    var attrs: List<Attribute>? = null
}

class UpdateMicAttrParams {
    var roomId = ""
    var uid = ""
    var type = "";
    var attrs: List<Attribute>? = null
}