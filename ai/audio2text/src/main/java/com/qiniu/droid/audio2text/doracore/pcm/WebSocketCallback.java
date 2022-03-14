package com.qiniu.droid.audio2text.doracore.pcm;


import com.qiniu.droid.audio2text.audio.QNAudioToText;

public interface WebSocketCallback {

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
