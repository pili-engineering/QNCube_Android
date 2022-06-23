package com.niucube.rtm

import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


suspend fun RtmAdapter.leaveChannel(channelId: String) = suspendCoroutine<Unit> { continuation ->
    leaveChannel(channelId, object : RtmCallBack {
        override fun onSuccess() {
            continuation.resume(Unit)
        }

        override fun onFailure(code: Int, msg: String) {
            Log.d("rtm", "leaveChannel onFailure ${code} ${msg}")
            continuation.resume(Unit)
        }
    })
}

suspend fun RtmAdapter.joinChannel(channelId: String) = suspendCoroutine<Unit> { continuation ->
    joinChannel(channelId, object : RtmCallBack {
        override fun onSuccess() {
            continuation.resume(Unit)
        }

        override fun onFailure(code: Int, msg: String) {
            continuation.resumeWithException(RtmException(code, msg))
        }
    })
}


suspend fun RtmAdapter.sendChannelMsg(msg: String, channelId: String, isDispatchToLocal: Boolean) =
    suspendCoroutine<Unit> { continuation ->

        sendChannelMsg(msg, channelId, isDispatchToLocal, object : RtmCallBack {
            override fun onSuccess() {
                continuation.resume(Unit)
            }

            override fun onFailure(code: Int, msg: String) {
                continuation.resumeWithException(RtmException(code, msg))
            }
        })
    }

suspend fun RtmAdapter.sendC2cMsg(msg: String, peerId: String, isDispatchToLocal: Boolean) =
    suspendCoroutine<Unit> { continuation ->
        sendC2cMsg(msg, peerId, isDispatchToLocal, object : RtmCallBack {
            override fun onSuccess() {
                continuation.resume(Unit)
            }

            override fun onFailure(code: Int, msg: String) {
                continuation.resumeWithException(RtmException(code, msg))
            }
        })
    }

class RtmException(val code: Int, val msg: String) : Exception(msg)