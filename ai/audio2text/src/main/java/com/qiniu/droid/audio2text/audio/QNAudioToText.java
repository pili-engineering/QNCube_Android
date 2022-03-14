package com.qiniu.droid.audio2text.audio;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 语音识别结果
 */
public class QNAudioToText {
    /**
     * 服务端生成的uuid
     */
    public String uuid;
    /**
     * 是否是websocket最后一条数据,0:非最后一条数据,1: 最后一条数据。在客户端发送"EOS"(即请求关闭websocket) 后服务端响应的标识。
     */
    public int ended;
    /**
     * 分片结束,当前消息的transcript为该片段最终结果，否则为partial结果
     */
    @SerializedName("final")
    public int finalX;
    /**
     * 语音的文本, 如果final=0, 则为partinal结果 (后面可能会更改),final=1为该片段最终结果
     */
    public String transcript;

    /**
     * 该文本所在的切片的起点(包含), 否则为-1
     */
    @SerializedName("start_seq")
    public int startSeq;
    /**
     * 为该文本所在的切片的终点(包含)，否则为-1
     */
    @SerializedName("end_seq")
    public int endSeq;
    /**
     * 该片段的起始时间，毫秒
     */
    @SerializedName("start_time")
    public double startTime;
    /**
     * 该片段的终止时间，毫秒
     */
    @SerializedName("end_time")
    public double endTime;
    /**
     * 是否分段开始: 1:是; 0:不是。 一般分段后返回
     */
    @SerializedName("seg_begin")
    public int segBegin;
    /**
     * partial结果文本, 开启needpartial后返回
     */
    @SerializedName("partial_transcript")
    public String partialTranscript;
    /**
     * 是否是vad分段开始说话的开始1:是分段开始说话; 0:不是。 注意，每个分段只提醒一次
     */
    @SerializedName("spk_begin")
    public int spkBegin;
    /**
     * 是否是vad分段开始说话的开始1:是分段开始说话; 0:不是。 注意，每个分段只提醒一次
     */
    @SerializedName("seg_index")
    public int segIndex;
    /**
     * 是否长时间静音，0:否;1:是
     */
    @SerializedName("long_sil")
    public int longSil;
    /**
     * 返回词语的对齐信息, 参数need_words=1时返回详细内存见下表。
     */

    public List<WordsDTO> words;


    public static class WordsDTO {
        /**
         * word : 一
         * seg_start : 0
         * voice_start : 6.46
         * seg_end : 2.58
         * voice_end : 9.04
         */

        /**
         * 词语本身，包括标点符号
         */
        public String word;
        /**
         * 该词语相对当前分段的起始时间, 毫秒
         */
        @SerializedName("seg_start")
        public double segStart;
        /**
         * 该词语相对当前分段的终止时间, 毫秒
         */
        @SerializedName("voice_start")
        public double voiceStart;
        /**
         * 该词语相对整个数据流的起始时间, 毫秒
         */
        @SerializedName("seg_end")
        public double segEnd;
        /**
         * 该词语相对整个数据流的终止时间, 毫秒
         */
        @SerializedName("voice_end")
        public double voiceEnd;
    }
}
