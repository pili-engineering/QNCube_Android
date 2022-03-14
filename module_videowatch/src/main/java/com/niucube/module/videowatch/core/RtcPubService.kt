package com.niucube.module.videowatch.core

import android.util.Base64
import android.util.Log
import com.qiniu.comp.network.NetBzException
import com.qiniu.comp.network.RetrofitManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class RtcPubService {

    var mCurrentTrackID = ""
        private set

     fun createPubJob(token: String, videoUrl: String) {
      
            val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
            val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
            val json: JSONObject = JSONObject(b64)

            val mAppId = json.optString("appId")
            val mRoomId = json.optString("roomName")
            val mUserId = json.optString("userId")

            val url =
                "https://rtc.qiniuapi.com/v3/apps/${mAppId}/rooms/${mRoomId}/pub?token=${token}"
            val type = "application/json;charset=utf-8".toMediaType()

            val body = "{\"sourceUrls\":[\"${videoUrl}\"]}".toString().toRequestBody(type)
            val response = RetrofitManager.postJsonUserExtraClient(url, body)
            val code = response.code
            if (code != 200) {
               throw (NetBzException(code, response.message))
            }
            val responseStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseStr)
            val tractId = jsonResp.optString("taskID")
            mCurrentTrackID = tractId
            Log.d(
                "RtcPubService",
                "RtcPubService  tractId  " + mCurrentTrackID + "   roomName  " + mRoomId
            )
        }

     fun deletePubJob(token: String) {
            val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
            val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
            val json: JSONObject = JSONObject(b64)

            val mAppId = json.optString("appId")
            val mRoomId = json.optString("roomName")
            val mUserId = json.optString("userId")
            val url =
                "https://rtc.qiniuapi.com/v3/apps/${mAppId}/rooms/${mRoomId}/pub/${mCurrentTrackID}?token=${token}"
            val response = RetrofitManager.delete(url)
            val code = response.code
            if (code != 200) {
               throw (NetBzException(code, response.message))
            } else {
                mCurrentTrackID = ""
            }
        }

     fun startPubJob(token: String) {
   
            val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
            val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
            val json: JSONObject = JSONObject(b64)

            val mAppId = json.optString("appId")
            val mRoomId = json.optString("roomName")
            val mUserId = json.optString("userId")

            val url =
                "https://rtc.qiniuapi.com/v3/apps/${mAppId}/rooms/${mRoomId}/pub/${mCurrentTrackID}/start?token=${token}"
            val type = "application/json;charset=utf-8".toMediaType()
            val body = "{}".toString().toRequestBody(type)
            val response = RetrofitManager.postJsonUserExtraClient(url, body)
            val code = response.code
            if (code != 200) {
               throw (NetBzException(code, response.message))
            }
        }

     fun stopPubJob(token: String) {
            val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
            val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
            val json: JSONObject = JSONObject(b64)

            val mAppId = json.optString("appId")
            val mRoomId = json.optString("roomName")
            val mUserId = json.optString("userId")

            val url =
                "https://rtc.qiniuapi.com/v3/apps/${mAppId}/rooms/${mRoomId}/pub/${mCurrentTrackID}/stop?token=${token}"
            val type = "application/json;charset=utf-8".toMediaType()
            val body = "{}".toString().toRequestBody(type)
            val response = RetrofitManager.postJsonUserExtraClient(url, body)
            val code = response.code
            if (code != 200) {
               throw (NetBzException(code, response.message))
            }
        }

     fun seekPubJob(token: String, seek: Int) {
            val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
            val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
            val json: JSONObject = JSONObject(b64)

            val mAppId = json.optString("appId")
            val mRoomName = json.optString("roomName")
            val mUserId = json.optString("userId")

            val url =
                "https://rtc.qiniuapi.com/v3/apps/${mAppId}/rooms/${mRoomName}/pub/${mCurrentTrackID}/seek?token=${token}"
            val type = "application/json;charset=utf-8".toMediaType()
            val body = "{\"index\":0,\"seek\":$seek}".toString().toRequestBody(type)
            val response = RetrofitManager.postJsonUserExtraClient(url, body)
            val code = response.code
            if (code != 200) {
               throw (NetBzException(code, response.message))
            }
        }

}