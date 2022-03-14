package com.qiniu.droid.audio2text.doracore.pcm

import android.util.Log
import com.qiniu.droid.audio2text.audio.QNAudioToText
import com.qiniu.droid.audio2text.audio.QNAudioToTextParam
import kotlinx.coroutines.*
import java.lang.Exception
import java.nio.ByteBuffer

/**
 * 音频转文字
 * @param call 音频识别回调
 */
class PcmAudio2Text(
    val call: WebSocketCallback
) {

    /**
     * 采样率
     */
    var sampleRate: Int = 0

    /**
     * 连接成功
     */
    @Volatile
    var isConnected = false
        private set

    @Volatile
    var isStarting = false
        private set

    /**
     * 语音识别长链接
     */
    private val mTranslateWebSocket: TranslateWebSocket by lazy {
        TranslateWebSocket(object : WebSocketCallback {
            override fun onStart() {
                call.onStart()
             //   bufferSend.clear()
                isConnected = true
                startBuffer()
            }

            override fun onError(code: Int, msg: String) {
                call.onError(code, msg)
                isStarting = false
                isConnected = false
            }

            override fun onStop() {
                call.onStop()
                isStarting = false
                isConnected = false
            }

            override fun onAudioToText(audioToText: QNAudioToText) {
                call.onAudioToText(audioToText)
            }
        })
    }

  //  private var job: Job? = null
  //  private var bufferSend = ArrayList<ByteArray>()

    /**
     * 缓冲一段音频数据
     */
    private fun startBuffer() {
//        job = GlobalScope.launch {
//            try {
//                while (isConnected) {
//                    delay(200)
//                    val bufferSendTemp = ArrayList<ByteArray>()
//                    bufferSendTemp.addAll(bufferSend)
//                    if (!bufferSendTemp.isEmpty()) {
//                        bufferSend.clear()
//                        var size = 0
//                        bufferSendTemp.forEach {
//                            size += it.size
//                        }
//                        val sendArray = ByteArray(size)
//                        var nextOff = 0
//                        bufferSendTemp.forEach {
//                            System.arraycopy(it, 0, sendArray, nextOff, it.size)
//                            nextOff += it.size
//                        }
//                        mTranslateWebSocket.sendFrame(sendArray)
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
    }

    /**
     * 开始连接
     * @param params  设别规则参数 不传则使用默认值
     * @param connectCall 开始连接 成功失败回调
     */
    fun start(
        params: QNAudioToTextParam = QNAudioToTextParam()
    ) {
        Log.d("TranslateWebSocket", "codisstart(\n" +
                "        params: QNAudioToTextParam ")

        if (isStarting || isConnected || sampleRate <= 0) {
            Log.d("TranslateWebSocket", "codisStarting || isConnected || sampleRate <= 0 start")

            return
        }
        isStarting = true
        GlobalScope.launch(Dispatchers.Main) {
            mTranslateWebSocket.startConnect(params, sampleRate)
        }
    }

    /**
     * 停止连接
     */
    fun stop() {
        if (!isConnected) {
            return
        }
        isStarting = false
        isConnected = false
        mTranslateWebSocket.sendEos()
      //  bufferSend.clear()
    }

    var dst:ByteArray? = null
    private var tempFrameSize = 20 // 一帧音频数据320比特
    private var currentFrameSize = 0

    /**
     * 收到trc单帧数据
     * @param audioData 音频pcm数据
     * @param size 数据大小
     * @param sampleRate 采样率
     */
    fun onAudioAvailable(
        audioData: ByteBuffer,
        size: Int,
        sampleRate: Int = 0
    ) {
        try {
            if (sampleRate != 0) {
                this.sampleRate = sampleRate
            }
            if (!isConnected) {
                return
            }
            if(dst == null){
                dst = ByteArray(size*tempFrameSize)
                currentFrameSize = 0
            }
            if(currentFrameSize>= tempFrameSize-1){
                mTranslateWebSocket.sendFrame(dst!!)
                currentFrameSize = 0
            }
            audioData[dst!!, size*(currentFrameSize), size]
            currentFrameSize++
            // bufferSend.add(dst)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

   // @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun onDestroy() {
//        mTranslateWebSocket.sendEos()
//        job?.cancel()
//    }

}