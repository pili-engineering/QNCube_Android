package com.qiniu.droid.audio2text.doracore.pcm

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.qiniu.droid.audio2text.QNRtcAiConfig
import com.qiniu.droid.audio2text.audio.QNAudioToText
import com.qiniu.droid.audio2text.audio.QNAudioToTextParam
import com.qiniu.droid.audio2text.doracore.JsonUtils
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

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
           "wss://ap-open-ws.service-z0.qiniuapp.com/asr?voice_type=1&model_type=${params.modelType}&voice_encode=1&voice_sample=${voice_sample}&needvad=${params.needVad}&need_partial=${params.needPartial}&maxsil=${params.maxSil}&need_words=${params.needWords}&force_final=${params.forceFinal}&vad_sil_thres=${params.vadSilThres}&e=${System.currentTimeMillis() / 1000}"
        if (!TextUtils.isEmpty(
                params.hotWords
            )
        ) {
            urlOrigin += "&hot_words=${UrlUtils.urlEncodeChinese(params.hotWords)}"
        }
        GlobalScope.launch(Dispatchers.Main) {
            val urlYns = async(Dispatchers.IO) {
                val url =
                    urlOrigin + "&token=${QNRtcAiConfig.signCallback?.signUrlToToken(urlOrigin)}"
                url
            }
            val url = urlYns.await()
            val serverURI: URI = URI.create(url)
            Log.d("TranslateWebSocket", "code mWebSocketClient mWebSocketClientmWebSocketClient start")
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

                private var lastMsg:QNAudioToText?=null
                override fun onMessage(message: String) {
                    JsonUtils.parseObjectOrigin(message, QNAudioToText::class.java)
                        ?.let {
                            //服务器确认了最后eos数据
                            if (it.ended == 1) {
                                //取消延迟任务
                                timeOutJob?.cancel()
                                //马上关闭
                                close()
                            }
                            if (it.transcript.isEmpty()) {
                                return
                            }
                            if (it.transcript == lastMsg?.transcript && it.finalX == lastMsg?.finalX) {
                                return
                            }
                            lastMsg = it
                            mHandler.post { call.onAudioToText(it) }
                        }
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
    fun sendFrame(bytes: ByteArray) {
       // Log.d("TranslateWebSocket", "code sendFrame")
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