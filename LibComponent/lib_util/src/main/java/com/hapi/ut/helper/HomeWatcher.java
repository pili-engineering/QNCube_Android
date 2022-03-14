/*
 * Copyright (c) 2017  athou（cai353974361@163.com）.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hapi.ut.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.ref.WeakReference;

/**
 * Home键监听
 *
 * @author xx
 */
public class HomeWatcher {

    static final String TAG = "HomeWatcher";
    private WeakReference<Context> mContext;
    private IntentFilter mFilter;
    private OnHomePressedListener mListener;
    private InnerRecevier mRecevier;

    public HomeWatcher(Context context) {
        mContext = new WeakReference<Context>(context);
        mRecevier = new InnerRecevier();
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }

    /**
     * 开始监听，注册广播
     */
    public void startWatch(OnHomePressedListener listener) {
        if (mRecevier != null) {
            mContext.get().registerReceiver(mRecevier, mFilter);
        }
        mListener = listener;
    }

    /**
     * 停止监听，注销广播
     */
    public void stopWatch() {
        if (mRecevier != null) {
            mContext.get().unregisterReceiver(mRecevier);
        }
        if (mListener != null) {
            mListener = null;
        }
    }

    /**
     * 广播接收者
     */
    class InnerRecevier extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        final String SYSTEM_DIALOG_REASON_HOME_KEY_LONG = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON);
                if (reason != null) {
                    if (mListener != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            // 短按home键
                            mListener.onHomePressed();
                        } else if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY_LONG)) {
                            // 长按home键
                            mListener.onHomeLongPressed();
                        }
                    }
                }
            }
        }
    }

    // 回调接口
    public interface OnHomePressedListener {
        /**
         * 短按home键
         */
        void onHomePressed();

        /**
         * 长按home键
         */
        void onHomeLongPressed();
    }
}