package com.qiniu.droid.audio2text.stt;

import com.qiniu.droid.Constants
import com.qiniu.droid.audio2text.AudioFrameIntercept
import com.qiniu.droid.audio2text.audio.QNAudioToText
import com.qiniu.droid.audio2text.audio.QNAudioToTextParam
import com.qiniu.droid.audio2text.util.backGround
import com.qiniu.droid.rtc.QNLocalAudioTrack
import com.qiniu.droid.rtc.QNTrack
import kotlinx.coroutines.*
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

    private val sendBuffer = ByteBuffer.allocateDirect(5 * 1024)

    //帧监听
    private val mAudioFrameIntercept = AudioFrameIntercept { srcBuffer: ByteBuffer,
                                                             size: Int,
                                                             bitsPerSample: Int,
                                                             sampleRate: Int,
                                                             numberOfChannels: Int ->
        onAudioAvailable(srcBuffer, size, sampleRate)
    }

    /**
     * 语音识别长链接
     */
    private val mTranslateWebSocket: TranslateWebSocket by lazy {
        TranslateWebSocket(object : WebSocketCallback {
            override fun onStart() {
                call.onStart()
                sendBuffer.clear()
                sendBuffer.rewind()
                isConnected = true
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

    /**
     * 开始连接
     * @param params  设别规则参数 不传则使用默认值
     * @param connectCall 开始连接 成功失败回调
     */
    fun start(
        audioTrack: QNLocalAudioTrack,
        params: QNAudioToTextParam = QNAudioToTextParam()
    ) {
        backGround {
            doWork {
                mAudioFrameIntercept.audioTrack = audioTrack
                mAudioFrameIntercept.run()
                var repeat = 0
                while (sampleRate <= 0 && repeat < 5) {
                    repeat++
                    delay(30)
                }
                if (sampleRate <= 0) {
                    call.onError(Constants.FRAME_TIME_OUT, Constants.AUDIO_FRAME_TIME_OUT_MSG)
                    return@doWork
                }
                if (isStarting || isConnected) {
                    return@doWork
                }
                isStarting = true
                mTranslateWebSocket.startConnect(params, sampleRate)
            }
        }
    }

    /**
     * 停止连接
     */
    fun stop() {
        if (!isConnected) {
            return
        }
        mAudioFrameIntercept.stop()
        isStarting = false
        isConnected = false
        mTranslateWebSocket.sendEos()
        sendBuffer.clear()
    }

    /**
     * 收到trc单帧数据
     * @param audioData 音频pcm数据
     * @param size 数据大小
     * @param sampleRate 采样率
     */
    private fun onAudioAvailable(
        audioData: ByteBuffer,
        size: Int,
        sampleRate: Int = 0
    ) {
        try {
            this.sampleRate = sampleRate
            if (!isConnected) {
                return
            }
            val dst = ByteArray(size)
            audioData[dst, 0, size]
            if (sendBuffer.position() + size > sendBuffer.capacity()) {
                sendBuffer.flip()
                mTranslateWebSocket.sendFrame(sendBuffer.array())
                sendBuffer.clear()
                sendBuffer.rewind()
                //Log.d("TranslateWebSocket","重写.put(dst)"+" position"+sendBuffer.position()+ " limit"+sendBuffer.limit())
            }
            sendBuffer.put(dst)
           // Log.d("TranslateWebSocket","sendBuffer.put(dst)"+" position"+sendBuffer.position()+ " limit"+sendBuffer.limit())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}