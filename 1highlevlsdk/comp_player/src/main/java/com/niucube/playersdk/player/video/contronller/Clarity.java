package com.niucube.playersdk.player.video.contronller;

/**
 * 清晰度
 */
public class Clarity {
    /**
     *    清晰度等级
      */
    public String gradeTip;
    /**
     * 270P、480P、720P、1080P、4K ...
     */
    public String gradeName;
    /**
     * 视频链接地址
     */
    public String videoUrl;

    public Clarity(String gradeTip, String gradeName, String videoUrl) {
        this.gradeTip = gradeTip;
        this.gradeName = gradeName;
        this.videoUrl = videoUrl;
    }
}