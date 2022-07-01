package com.niucube.basemutableroom

import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.qiniu.droid.rtc.*
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RtcException(val code: Int, val msg: String) : Exception(msg)

suspend fun RtcRoom.joinRtc(token: String, msg: String) =
    suspendCoroutine<Unit> { continuation ->
        val trackQNRTCEngineEvent = object : SimpleQNRTCListener {
            override fun onConnectionStateChanged(
                state: QNConnectionState,
                p1: QNConnectionDisconnectedInfo?
            ) {
                if (state == QNConnectionState.CONNECTED) {
                    removeExtraQNRTCEngineEventListener(this)
                    continuation.resume(Unit)
                }

                if (state == QNConnectionState.DISCONNECTED) {
                    removeExtraQNRTCEngineEventListener(this)
                    continuation.resumeWithException(
                        RtcException(
                            p1?.errorCode ?: 1,
                            p1?.errorMessage ?: ""
                        )
                    )
                }
            }
        }

        addExtraQNRTCEngineEventListener(trackQNRTCEngineEvent)
        mClient.join(token, msg)
    }


fun QNTrack.tryPlay( var1: QNRenderView){
    if(this is QNLocalVideoTrack){
        play(var1)
        return
    }
    if(this is QNRemoteVideoTrack){
        play(var1)
        return
    }
}