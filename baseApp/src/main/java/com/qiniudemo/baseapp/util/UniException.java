package com.qiniudemo.baseapp.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class UniException implements Thread.UncaughtExceptionHandler {

    public static final String TAG = "UniException";

    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    // UniException实例
    private static UniException INSTANCE = new UniException();

    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();

    /**
     * 保证只有一个实例
     */
    private UniException() {
    }

    /**
     * 获取UniException实例 ,单例模式
     */
    public static UniException getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UniException();
            INSTANCE.init();
        }
        return INSTANCE;
    }


    public void init() {
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该本类为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e("uncaughtException", "error : " + e.getMessage());
            }
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        ex.printStackTrace();
        return false;
    }


}
