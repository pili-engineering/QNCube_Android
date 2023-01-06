package com.qiniu.droid.audio2text.util

import com.google.gson.JsonParseException
import com.qiniu.droid.Constants
import kotlinx.coroutines.*
import org.webrtc.VideoFrame
import org.webrtc.YuvHelper
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.ByteBuffer

class CoroutineScopeWrap {
    var work: (suspend CoroutineScope.() -> Unit) = {}
    var error: (e: Throwable) -> Unit = {}
    var complete: () -> Unit = {}

    fun doWork(call: suspend CoroutineScope.() -> Unit) {
        this.work = call
    }

    fun catchError(error: (e: Throwable) -> Unit) {
        this.error = error
    }

    fun onFinally(call: () -> Unit) {
        this.complete = call
    }
}

fun backGround(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    c: CoroutineScopeWrap.() -> Unit
) {
    GlobalScope
        .launch(dispatcher) {
            val block = CoroutineScopeWrap()
            c.invoke(block)
            try {
                block.work.invoke(this)
            } catch (e: Exception) {
                e.printStackTrace()
                block.error.invoke(e)
            } finally {
                block.complete.invoke()
            }
        }
}

fun Throwable.getCode(): Int {
    if (this is JsonParseException) {
        return Constants.UNKNOWN_ERROR
    }
    if (this is UnknownHostException) {
        return -100
    }
    if (this is SocketTimeoutException) {
        return -101
    }
    return -1
}

