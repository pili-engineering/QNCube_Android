package com.qiniu.droid.audio2text.stt;

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.qiniu.droid.audio2text.QNRtcAiConfig
import com.qiniu.droid.audio2text.audio.QNAudioToText
import com.qiniu.droid.audio2text.audio.QNAudioToTextParam
import com.qiniu.droid.audio2text.util.JsonUtils
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

class TranslateWebSocket(var call: WebSocketCallback) {

    private var mWebSocketClient: WebSocketClient? = null
    var isConnect = false
        private set
    var isConnected = false
        private set
    private val mHandler = Handler(Looper.myLooper()!!)

    /**
     * 开始链接
     */
    fun startConnect(
        params: QNAudioToTextParam,
        voice_sample: Int
    ) {
        isConnect = true
        var urlOrigin =
            "wss://ap-open-ws.service-z0.qiniuapp.com/v2/asr?aue=1&voice_sample=${voice_sample}&model_type=${params.modelType}&voice_id=${params.voiceID}&e=${System.currentTimeMillis() / 1000}"
        if (!TextUtils.isEmpty(
                params.keyWords
            )
        ) {
            urlOrigin += "&hot_words=${UrlUtils.urlEncodeChinese(params.keyWords)}"
        }
        GlobalScope.launch(Dispatchers.Main) {
            val urlYns = async(Dispatchers.IO) {
                val url =
                    urlOrigin + "&token=${QNRtcAiConfig.signCallback?.signUrlToToken(urlOrigin)}"
                url
            }
            val url = urlYns.await()
            val serverURI: URI = URI.create(url)
            mWebSocketClient = object : WebSocketClient(serverURI) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d("TranslateWebSocket", "code onOpen")
                    isConnected = true
                    GlobalScope.launch(Dispatchers.Main) {
                        call.onStart()
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d("TranslateWebSocket", "code onClose")
                    isConnect = false
                    isConnected = false

                    GlobalScope.launch(Dispatchers.Main) {
                        if (1000 != code) {
                            call.onError(code, reason)
                        }
                        call.onStop()
                    }
                }

                override fun onMessage(bytes: ByteBuffer) {
                    super.onMessage(bytes)
                    val bytesArray  = bytes.array()
                    val message = String(bytesArray)
                    JsonUtils.parseObject(message, QNAudioToText::class.java)
                        ?.let {
                            mHandler.post { call.onAudioToText(it) }
                        }
                }

                override fun onMessage(message: String) {
                    Log.d("TranslateWebSocket", "onMessage  " + message)
                    JsonUtils.parseObject(message, QNAudioToText::class.java)
                        ?.let {
                            mHandler.post { call.onAudioToText(it) }
                        }
                }

                override fun onError(ex: Exception?) {
                    Log.d("TranslateWebSocket", "onError  " + ex?.message)
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
    fun sendFrame(bytes: ByteArray) {
     //   Log.d("TranslateWebSocket", "code sendFrame" + bytes.size)
        if (isConnected) {
            mWebSocketClient?.send(bytes)
        }
    }

    /**
     * 发送结束符号
     */
    fun sendEos() {
        Log.d("TranslateWebSocket", "code sendEos")
        if (mWebSocketClient?.isOpen == true) {
            mWebSocketClient?.send("EOS")
        }
        isConnect = false
        isConnected = false
        startCloseJob()
    }

    private var timeOutJob: Job? = null
    private fun startCloseJob() {
        timeOutJob?.cancel()
        // 延迟关闭链接任务  sendEos 后服务端还有没翻译完的数据  等待结束
        timeOutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(500)
            close()
        }
    }

    private fun close() {
        mWebSocketClient?.close()
        mWebSocketClient = null
    }
}