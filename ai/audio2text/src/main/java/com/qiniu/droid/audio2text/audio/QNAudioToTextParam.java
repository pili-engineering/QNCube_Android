package com.qiniu.droid.audio2text.audio;


/**
 * 语音识别参数
 */
public class QNAudioToTextParam {

    /**
     * 是否需要vad;0->关闭;1->开启; 默认1
     */
    public int needVad = 1;

    /**
     * 是否返回partial文本，1->返回，0-> 不返回;默认1
     */
    public int needPartial = 1;

    /**
     * 最长静音间隔，单位秒，默认10s
     */
    public int maxSil = 10;

    /**
     * 是否返回词语的对齐信息，1->返回， 0->不返回;默认0。 以字段words返回，列表格式。
     */
    public int needWords = 1;

    /**
     * 0->cn; 默认0
     */
    public int modelType = 0;

    /**
     * 是否在text为空的时候返回final信息, 1->强制返回;0->不强制返回。 默认情况下，如果text为空， 不会返回final信息
     */
    public int forceFinal = 1;

    /**
     * vad断句的累积时间，大于等于0， 如果设置为0，或者没设置，系统默认
     */
    public double vadSilThres = 0.5;

    /**
     * 提供热词，格式为: hot_words=热词1,因子1;热词2,因子2，每个热词由热词本身和方法因子以英文逗号隔开，不同热词通过;隔开，最多100个热词，每个热词40字节以内。由于潜在的http服务对url大小的限制，以实际支持的热词个数为准
     * 因子范围[-10,10], 正数代表权重权重越高，权重越高越容易识别成这个词，建议设置1 ，负数代表不想识别
     */
    public String hotWords;
}
