package com.hapi.ut.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import java.lang.ref.WeakReference;

/**
 * 监听手机屏幕开屏、锁屏、解锁等操作
 * @author xx
 */
public class ScreenWatcher {
    private WeakReference<Context> mContext;
    private ScreenBroadcastReceiver mScreenReceiver;
    private OnScreenStateListener mScreenStateListener;

    public ScreenWatcher(Context context) {
        mContext = new WeakReference<Context>(context);
        mScreenReceiver = new ScreenBroadcastReceiver();
    }

    /**
     * screen状态广播接收者
     */
    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // 开屏
                mScreenStateListener.onScreenOn();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // 锁屏
                mScreenStateListener.onScreenOff();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                // 解锁
                mScreenStateListener.onUserPresent();
            }
        }
    }

    /**
     * 开始监听screen状态
     *
     * @param listener
     */
    public void begin(OnScreenStateListener listener) {
        mScreenStateListener = listener;
        registerListener();
        getScreenState();
    }

    /**
     * 获取screen状态
     */
    private void getScreenState() {
        PowerManager manager = (PowerManager) mContext.get().getSystemService(Context.POWER_SERVICE);
        if (manager.isScreenOn()) {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenOn();
            }
        } else {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenOff();
            }
        }
    }

    /**
     * 停止screen状态监听
     */
    public void unregisterListener() {
        mContext.get().unregisterReceiver(mScreenReceiver);
    }

    /**
     * 启动screen状态广播接收器
     */
    private void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.get().registerReceiver(mScreenReceiver, filter);
    }

    public interface OnScreenStateListener {// 返回给调用者屏幕状态信息

        /**
         * 开屏
         */
        void onScreenOn();

        /**
         * 关屏
         */
        void onScreenOff();

        /**
         * 用户解锁
         */
        void onUserPresent();
    }
}