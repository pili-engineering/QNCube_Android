package com.qiniu.droid.audio2text

import android.util.Log
import com.qiniu.droid.rtc.*

import kotlinx.coroutines.*
import java.nio.ByteBuffer

private const val frameTimeOut = 200L;

/**
 * 音频帧拦截
 */
class AudioFrameIntercept(

    private val audioTrack: QNTrack,
    private val workCall: (
        srcBuffer: ByteBuffer,
        size: Int,
        bitsPerSample: Int,
        sampleRate: Int,
        numberOfChannels: Int
    ) -> Unit,
    private val timeOutCall: () -> Unit
) {

    var isStart = false

    private var timeOutJob: Job? = null

    private val mQNAudioSourceCallback by lazy {
        object : QNAudioFrameListener {

            override fun onAudioFrameAvailable(
                srcBuffer: ByteBuffer,
                size: Int,
                bitsPerSample: Int,
                sampleRate: Int,
                numberOfChannels: Int
            ) {
                if (!isStart) {
                    return
                }
                reStartTimeOutJob()
                workCall.invoke(srcBuffer, size, bitsPerSample, sampleRate, numberOfChannels)
            }
        }
    }

    private fun reStartTimeOutJob() {
        timeOutJob?.cancel()
        timeOutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(frameTimeOut)
            if (audioTrack is QNLocalAudioTrack) {
                audioTrack.setAudioFrameListener(null)
            }
            if (audioTrack is QNRemoteAudioTrack) {
                audioTrack.setAudioFrameListener(null)
            }
            Log.d("AudioFrameIntercept", "reStartTimeOutJob")
            timeOutCall.invoke()
        }
    }

    fun stop() {
        isStart = false
        timeOutJob?.cancel()
        if (audioTrack is QNLocalAudioTrack) {
            audioTrack.setAudioFrameListener(null)
        }
        if (audioTrack is QNRemoteAudioTrack) {
            audioTrack.setAudioFrameListener(null)
        }
        Log.d("AudioFrameIntercept", "stop")
    }

    fun run(
    ) {
        isStart = true
        reStartTimeOutJob()
        if (audioTrack is QNLocalAudioTrack) {
            audioTrack.setAudioFrameListener(mQNAudioSourceCallback)
        }
        if (audioTrack is QNRemoteAudioTrack) {
            audioTrack.setAudioFrameListener(mQNAudioSourceCallback)
        }
    }
}