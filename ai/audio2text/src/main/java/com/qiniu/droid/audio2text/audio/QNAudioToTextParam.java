package com.qiniu.droid.audio2text.audio;


/**
 * 语音识别参数
 */
public class QNAudioToTextParam {

    /**
     * 识别语言，中文: 1, 英文: 2, 中英混合: 0; 默认 1
     */
    public int modelType = 1;

    /**
     * 识别关键字; 相同读音时优先识别为关键字。每个词 2-4 个字, 不同词用 , 分割
     */
    public String keyWords = "";

    /**
     * 流ID
     * 用于排查日志
     */
    public String voiceID = "";

}
