package com.qiniu.droid.audio2text;

import android.util.Log
import com.qiniu.droid.rtc.QNAudioFrameListener
import com.qiniu.droid.rtc.QNLocalAudioTrack
import java.nio.ByteBuffer

/**
 * 音频帧拦截
 */
class AudioFrameIntercept(
    private val workCall: (
        srcBuffer: ByteBuffer,
        size: Int,
        bitsPerSample: Int,
        sampleRate: Int,
        numberOfChannels: Int
    ) -> Unit
) {
    var isStart = false
    var audioTrack: QNLocalAudioTrack? = null
    private val mQNAudioDataCallback: QNAudioFrameListener =
        QNAudioFrameListener { byteBuffer, var2, var3, var4, var5 ->
            if (!isStart) {
                return@QNAudioFrameListener
            }
            workCall.invoke(byteBuffer, var2, var3, var4, var5)
        }

    fun stop() {
        isStart = false
        audioTrack?.setAudioFrameListener(null)
        Log.d("AudioFrameIntercept", "removeAudioDataCallback")
        audioTrack = null
    }

    fun run() {
        audioTrack?.setAudioFrameListener(null)
        isStart = true
        audioTrack?.setAudioFrameListener(mQNAudioDataCallback)
    }
}