package com.niucube.module.videowatch.core

import android.util.Base64
import android.util.Log
import com.niucube.comproom.RoomManager
import com.niucube.lazysitmutableroom.LazySitMutableLiverRoom
import com.niucube.lazysitmutableroom.LazySitUserMicSeat
import com.niucube.lazysitmutableroom.UserMicSeatListener
import com.niucube.basemutableroom.adminTrack.AdminTrackManager
import com.qiniu.droid.rtc.QNRemoteTrack
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.been.hostId
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

class VideoMixHelper {

    private val mMixStreamWebSocket = MixStreamWebSocket()
    private fun saveGreenMattingPullUri(
        token: String,
        rtcPubId: String,
        userLeft: String,
        userRight: String
    ) {
        val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
        val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
        val json: JSONObject = JSONObject(b64)

        val mAppId = json.optString("appId")
        val mRoomId = json.optString("roomName")
        val mUserId = json.optString("userId")

        val mixParams = MixParams()
        mixParams.outputs.add(MixParams.OutputsDTO().apply {
            url = RoomManager.mCurrentRoom?.providePullUri()
        })
        mixParams.inputs.add(MixParams.InputsDTO().apply {
            url =  "rtc://${mAppId}/${mRoomId}/${rtcPubId}?prefix=1"
            x = 0;
            y = 0;
            w = 1080;
            h = 720;
            filter = "RobustVideoMatting"
        })
        mixParams.inputs.add(MixParams.InputsDTO().apply {
            url = "rtc://${mAppId}/${mRoomId}/${userLeft}"
            x = 150;
            y = 150;
            w = 200;
            h = 200;
            filter = "ChromaKey"
        })
        if (userRight.isNotEmpty()) {
            mixParams.inputs.add(MixParams.InputsDTO().apply {
                url = "rtc://${mAppId}/${mRoomId}/${userRight}"
                x = 450;
                y = 450;
                w = 200;
                h = 200;
                filter = "RobustVideoMatting"
            })
        }
        mMixStreamWebSocket.sendFrame(JsonUtils.toJson(mixParams))
    }

    private lateinit var mPubService: RtcPubService
    private var rtcPubId: String = ""
    private var userLeft: String = ""
    private var userRight: String = ""

    private fun checkSaveGreenMattingPullUri() {
        if (rtcPubId.isNotEmpty() && userLeft.isNotEmpty()) {
            saveGreenMattingPullUri(
                RoomManager.mCurrentRoom?.provideRoomToken() ?: "", rtcPubId,
                userLeft, userRight
            )
        }
    }

    fun attach(rtcRoom: LazySitMutableLiverRoom, pubService: RtcPubService) {
        mMixStreamWebSocket.startConnect()

        mPubService = pubService
        //  rtcRoom.getMixStreamHelper().setMixParams(mMixStreamParams)
        rtcRoom.adminTrackManager.addAdminTrackListener(object :
            AdminTrackManager.AdminTrackListener {
            override fun onAdminPubAudio(track: QNRemoteTrack): Boolean {
                //  rtcRoom.getMixStreamHelper().updateAudioMergeOptions(track.trackID, true)
                return super.onAdminPubAudio(track)
            }

            override fun onAdminPubVideo(track: QNRemoteTrack): Boolean {
//                rtcRoom.getMixStreamHelper()
//                    .updateVideoMergeOptions(track.trackID, videoMergeTrackOption)
                //rtcPubId =  track.userID
                if(rtcPubId== track.userID){
                    return super.onAdminPubAudio(track)
                }
                rtcPubId =  track.userID

                checkSaveGreenMattingPullUri()
                Log.d("RtcPubService", "onAdminPubVideo  tractId" + track.userID)
                return super.onAdminPubAudio(track)
            }
        })

        rtcRoom.addUserMicSeatListener(object : UserMicSeatListener {
            override fun onUserSitDown(micSeat: LazySitUserMicSeat) {
                if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                    userLeft = micSeat.uid
                    checkSaveGreenMattingPullUri()
                } else {
                    userRight = micSeat.uid
                    checkSaveGreenMattingPullUri()
                }
            }

            override fun onUserSitUp(micSeat: LazySitUserMicSeat, isOffLine: Boolean) {
                if (micSeat.uid == RoomManager.mCurrentRoom?.hostId()) {
                } else {
                    userRight = ""
                    checkSaveGreenMattingPullUri()
                }
            }

            override fun onCameraStatusChanged(micSeat: LazySitUserMicSeat) {}
            override fun onMicAudioStatusChanged(micSeat: LazySitUserMicSeat) {}
        })
    }


}


class MixStreamWebSocket() {

    private var mWebSocketClient: WebSocketClient? = null
    var isConnect = false
        private set
    var isConnected = false
        private set

    var unSendMsg=""

    /**
     * 开始链接
     */
    fun startConnect() {

        isConnect = true
        var urlOrigin = "wss://merger-test.cloudvdn.com:11111/ws"

        GlobalScope.launch(Dispatchers.Main) {

            val serverURI: URI = URI.create(urlOrigin)
            mWebSocketClient = object : WebSocketClient(serverURI) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d("MixStreamWebSocket", "code onOpen")
                    if(unSendMsg.isNotEmpty()){
                        send(unSendMsg)
                        unSendMsg=""
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d("MixStreamWebSocket", "code onClose")
                    isConnect = false
                    isConnected = false
                    mWebSocketClient?.connect()
                }

                override fun onMessage(message: String) {
                    Log.d("MixStreamWebSocket", "onMessage ${message}")
                }

                override fun onError(ex: Exception?) {
                    isConnect = false
                    isConnected = false
                    ex?.printStackTrace()
                }
            }
            mWebSocketClient?.connect()
        }
    }

    /**
     * 发送音频数据
     */
    fun sendFrame(msg: String) {
         Log.d("MixStreamWebSocket", "code sendFrame  ${msg}")
//        if (isConnected) {
//            mWebSocketClient?.send(msg)
//            unSendMsg=""
//        } else {
//            unSendMsg = msg
//        }
    }

    private fun close() {
        mWebSocketClient?.close()
        mWebSocketClient = null
    }
}