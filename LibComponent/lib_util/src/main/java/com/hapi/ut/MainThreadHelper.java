package com.hapi.ut;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xx
 * @date 5/19/16
 */
public class MainThreadHelper {

    private static Handler handler;
    private static ExecutorService executors = Executors.newCachedThreadPool();

    private MainThreadHelper() {
    }

    public static Handler getHandler() {
        if (handler == null) {
            synchronized (MainThreadHelper.class) {
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return handler;
    }

    public static void assertMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("Assertion failed: must be in main thread.");
        }
    }

    public static void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MainThreadHelper.post(runnable);
        } else {
            runnable.run();
        }
    }

    public static void postAsync(Runnable runnable) {
        executors.execute(runnable);
    }

    public static void post(Runnable runnable) {
        getHandler().post(runnable);
    }

    public static void postDelayed(Runnable runnable, long delayMillis) {
        getHandler().postDelayed(runnable, delayMillis);
    }

    public static void removeCallbacks(Runnable runnable) {
        getHandler().removeCallbacks(runnable);
    }
}