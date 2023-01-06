package com.qiniu.droid.audio2text.audio;

import java.io.Serializable;
import java.util.List;

/**
 * 语音识别结果
 */
public class QNAudioToText implements Serializable {

    /**
     * 此识别结果是否为最终结果
     */
    public boolean isFinal;
    /**
     * 此识别结果是否为第一片
     */
    public boolean isBegin;
    /**
     * 最好的转写候选
     */
    public BestTranscription bestTranscription;

    /**
     * 最好的转写候选
     */
    public static class BestTranscription implements Serializable {

        /**
         * 转写结果
         */
        public String transcribedText;
        /**
         * 句子的开始时间, 单位毫秒
         */
        public int beginTimestamp;
        /**
         * 句子的结束时间, 单位毫秒
         */
        public int endTimestamp;
        /**
         * 转写结果中包含KeyWords内容
         */
        public List<KeyWordsType> keyWordsType;
        /**
         * 转写结果的分解（只对final状态结果有效，返回每个字及标点的详细信息）
         */
        public List<Piece> piece;

        /**
         * 结果中包含KeyWords
         */
        public static class KeyWordsType implements Serializable {
            /**
             * 命中的关键词KeyWords。返回不多于10个。
             */
            public String keyWords;
            /**
             * 命中的关键词KeyWords相应的分数。分数越高表示和关键词越相似，对应kws中的分数。
             */
            public double keyWordsScore;
            /**
             * 关键词开始时间, 单位毫秒
             */
            public int startTimestamp;
            /**
             * 关键词结束时间, 单位毫秒
             */
            public int endTimestamp;
        }

        /**
         * 转写结果的分解（只对final状态结果有效，返回每个字及标点的详细信息）
         */
        public static class Piece implements Serializable {

            /**
             * 转写分解结果
             */
            public String transcribedText;
            /**
             * 分解开始时间(音频开始时间为0), 单位毫秒
             */
            public int beginTimestamp;
            /**
             * 分解结束时间(音频开始时间为0), 单位毫秒
             */
            public int endTimestamp;
        }
    }
}
