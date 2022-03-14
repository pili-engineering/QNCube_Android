package com.hapi.ut.constans;

/**
 * app全局变量，包含配置变量
 */
public class Constants {
    /**
     * 日志开关.
     */
    public static boolean DEBUG = false;

    /**
     * 性能测试.
     */
    public static boolean mMonitorOpened = false;

    /**
     * 起始执行时间.
     */
    public static long startLogTimeInMillis = 0;

    /**
     * UI设计的基准宽度.
     */
    public static int UI_WIDTH = 720;

    /**
     * UI设计的基准高度.
     */
    public static int UI_HEIGHT = 1280;

    /**
     * UI设计的密度.
     */
    public static int UI_DENSITY = 2;

    /**
     * 文件大小描述
     */
    public static final int KB = 1024;
    public static final int MB = 1024 * 1024;

    /**
     * 默认超时时间
     */
    public static final int DEFAULT_TIME_OUT = 5 * 1000;

    /**
     * web空url
     */
    public static final String URL_EMPTY = "about:blank";
}
