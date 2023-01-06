package com.qiniu.droid.audio2text;


import com.qiniu.droid.audio2text.audio.QNAudioToText;
import com.qiniu.droid.audio2text.audio.QNAudioToTextParam;
import com.qiniu.droid.audio2text.stt.PcmAudio2Text;
import com.qiniu.droid.audio2text.stt.WebSocketCallback;
import com.qiniu.droid.rtc.QNLocalAudioTrack;
import com.qiniu.droid.rtc.QNTrack;

import org.jetbrains.annotations.Nullable;

/**
 * 语音转文字
 */
public class QNAudioToTextAnalyzer {

    /**
     * 开始语音实时识别
     *
     * @param audioTrack 音频轨道
     * @param param      视频活体动作检测参数,null 则用默认值
     * @param callback   回调函数
     * @return 语音转文字分析器
     */
    public static QNAudioToTextAnalyzer start(QNLocalAudioTrack audioTrack, @Nullable QNAudioToTextParam param, QNAudioToTextCallback callback) {
        return new QNAudioToTextAnalyzer(audioTrack, param, callback);
    }

    //编码器
    private PcmAudio2Text audio2Text = null;

    private QNAudioToTextAnalyzer(QNLocalAudioTrack audioTrack, @Nullable QNAudioToTextParam param, QNAudioToTextCallback callback) {
        if (param == null) {
            param = new QNAudioToTextParam();
        }
        audio2Text = new PcmAudio2Text(new WebSocketCallback() {
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
        audio2Text.start(audioTrack, finalParam);
    }

    /**
     * 停止语音实时识别
     */
    public void stop() {
        if (audio2Text != null) {
            audio2Text.stop();
        }
        audio2Text = null;
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
