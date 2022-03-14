package com.qiniu.droid.audio2text;

import android.util.Log;

import com.qiniu.droid.audio2text.audio.QNAudioToText;
import com.qiniu.droid.audio2text.audio.QNAudioToTextParam;
import com.qiniu.droid.audio2text.doracore.pcm.PcmAudio2Text;
import com.qiniu.droid.audio2text.doracore.pcm.WebSocketCallback;
import com.qiniu.droid.rtc.QNTrack;

import org.jetbrains.annotations.Nullable;

/**
 * 语音转文字
 */
public class QNAudioToTextAnalyzer {

    /**
     * 开始语音实时识别自己
     *
     * @param param      视频活体动作检测参数,null 则用默认值
     * @param callback   回调函数
     * @return 语音转文字分析器
     */

    public static QNAudioToTextAnalyzer start(QNTrack audioTrack, @Nullable QNAudioToTextParam param, QNAudioToTextCallback callback) {
        return new QNAudioToTextAnalyzer( audioTrack,param, callback);
    }
    //帧监听
    private AudioFrameIntercept mAudioFrameIntercept = null;
    //编码器
    private PcmAudio2Text qiniuPcmAudio2Text = null;

    private QNAudioToTextAnalyzer(QNTrack audioTrack, @Nullable QNAudioToTextParam param, QNAudioToTextCallback callback) {
        if (param == null) {
            param = new QNAudioToTextParam();
        }
        qiniuPcmAudio2Text = new PcmAudio2Text(new WebSocketCallback() {
            @Override
            public void onStart() {
                callback.onStart();
            }

            @Override
            public void onError(int code, String msg) {
                callback.onError(code, msg);
            }

            @Override
            public void onStop() {
                callback.onStop();
            }

            @Override
            public void onAudioToText(QNAudioToText audioToText) {
                callback.onAudioToText(audioToText);
            }
        });

        QNAudioToTextParam finalParam = param;
        mAudioFrameIntercept = new AudioFrameIntercept(
                audioTrack,
                (byteBuffer,
                 size,
                 bitsPerSample,
                 sampleRate,
                 numberOfChannels) -> {

                    if (qiniuPcmAudio2Text == null) {
                        return null;
                    }
                    qiniuPcmAudio2Text.onAudioAvailable(byteBuffer, size, sampleRate);
                    if (!qiniuPcmAudio2Text.isStarting()) {
                        qiniuPcmAudio2Text.start(finalParam);
                    }
                    return null;
                }, () -> {
            qiniuPcmAudio2Text.stop();
            Log.d("AudioFrameIntercept", "ontimeoutFrame");
            callback.onStop();
            callback.onError(Constants.FRAME_TIME_OUT, Constants.AUDIO_FRAME_TIME_OUT_MSG);

            return null;
        });
        mAudioFrameIntercept.run();
    }

    /**
     * 停止语音实时识别
     */
    public void stop() {
        if(qiniuPcmAudio2Text!=null){
            qiniuPcmAudio2Text.stop();
        }
        if(mAudioFrameIntercept!=null){
            mAudioFrameIntercept.stop();
        }
        qiniuPcmAudio2Text = null;
        mAudioFrameIntercept = null;
    }

    /**
     * 实时语音转文字回调
     */
    public static interface QNAudioToTextCallback {

        /**
         * 开始成功
         */
        void onStart();

        /**
         * 错误
         *
         * @param code 错误码
         * @param msg  错误提示
         */
        void onError(int code, String msg);

        /**
         * 实时转化结束
         */
        void onStop();

        /**
         * 实时转化文字数据
         *
         * @param audioToText 当前片段的结果文字数据
         */
        void onAudioToText(QNAudioToText audioToText);
    }


}
