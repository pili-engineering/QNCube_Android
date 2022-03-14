package com.qiniu.droid.audio2text;

public class Constants {

    /**
     * 请求成功
     */
    public static final int RESULT_OK = 0;

    /**
     *未知异常
     */
    public static final int  UNKNOWN_ERROR=1000;

    /**
     * 视频/音频帧超时 （轨道退订或者已经销毁）
     */
    public static final int FRAME_TIME_OUT=1001;
    /**
     * 过期 token
     */
    public static final int TOKEN_EXCEED=1003;

    public static final String VIDEO_FRAME_TIME_OUT_MSG="video frame timeout";
    public static final String AUDIO_FRAME_TIME_OUT_MSG="audio frame timeout";

}
