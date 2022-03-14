/*
 * Copyright (c) 2016  athou（cai353974361@163.com）.
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

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.RequiresPermission;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Stack;

/**
 * activity管理类
 */
public class ActivityManager {

    private static Stack<WeakReference<Activity>> activityStack;
    private static ActivityManager instance;

    private ActivityManager() {
        activityStack = new Stack<WeakReference<Activity>>();
    }

    public static ActivityManager get() {
        synchronized (ActivityManager.class) {
            if (instance == null) {
                instance = new ActivityManager();
            }
            return instance;
        }
    }

    public void init(Application application) {
        application.registerActivityLifecycleCallbacks(new InnerActivityLifecycle());
    }

    /**
     * 获取activity个数
     *
     * @return
     */
    public int size() {
        if (activityStack == null) {
            activityStack = new Stack<WeakReference<Activity>>();
        }
        return activityStack.size();
    }

    /**
     * 是否有这个activity（这个activity是否在activity栈内）
     *
     * @param cls
     * @return
     */
    public boolean hasActivity(Class<? extends Activity> cls) {
        for (int i = 0; i < activityStack.size(); i++) {
            if (activityStack.get(i) != null & activityStack.get(i).get() != null && activityStack.get(i).get().getClass().equals(cls)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取指定的Activity
     */
    public static Activity getActivity(Class<? extends Activity> cls) {
        WeakReference<Activity> weakReference = findWeakByActivityCls(cls);
        if (weakReference != null) {
            return weakReference.get();
        }
        return null;
    }

    private static WeakReference<Activity> findWeakByActivity(Activity activity) {
        for (WeakReference<Activity> weakReference : activityStack) {
            if (weakReference.get() != null && weakReference.get() == activity) {
                return weakReference;
            }
        }
        return null;
    }

    private static WeakReference<Activity> findWeakByActivityCls(Class<? extends Activity> activityClass) {
        for (WeakReference<Activity> weakReference : activityStack) {
            if (weakReference.get() != null && weakReference.get().getClass().equals(activityClass)) {
                return weakReference;
            }
        }
        return null;
    }

    /**
     * 获取当前activity
     *
     * @return
     */
    public Activity currentActivity() {
        if (!activityStack.isEmpty()) {
            WeakReference<Activity> temp = activityStack.lastElement();
            if (temp.get() != null) {
                return temp.get();
            }
        }
        return null;
    }

    /**
     * 销毁栈顶的activity
     */
    public void finishTopActivity() {
        WeakReference<Activity> activity = activityStack.lastElement();
        if (activity != null && activity.get() != null) {
            activity.get().finish();

            activityStack.remove(activity);
        }
    }

    /**
     * 销毁指定的activity
     *
     * @param activityClass
     */
    public void finishActivity(Class<? extends Activity> activityClass) {
        for (int i = 0; i < activityStack.size(); i++) {
            WeakReference<Activity> activity = activityStack.elementAt(i);
            if (activity != null && activity.get() != null && activity.get().getClass().equals(activityClass)) {
                finishActivity(activity.get());
                return;
            }
        }
    }

    /**
     * 销毁指定的activity
     *
     * @param activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            WeakReference<Activity> weakReference = findWeakByActivity(activity);
            if (weakReference != null) {
                if (weakReference.get() != null) {
                    weakReference.get().finish();
                }
                activityStack.remove(weakReference);
            }
        }
    }

    /**
     * 销毁所有activity
     */
    public void finishAllActivity() {
        if (activityStack == null || activityStack.isEmpty()) {
            return;
        }
        for (WeakReference<Activity> weakReference : activityStack) {
            if (weakReference != null && weakReference.get() != null) {
                weakReference.get().finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 销毁所有activity，保留最开始的activity
     */
    public void popAllActivityKeepTop() {
        while (true) {
            if (size() <= 1) {
                break;
            }
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            finishActivity(activity);
        }
    }

    /**
     * 销毁所有activity，保留指定activity
     *
     * @param cls
     */
    public void popAllActivityExceptOne(Class<? extends Activity> cls) {
        if (activityStack.isEmpty()) {
            return;
        }
        int i = 0;
        while (true) {
            int size = activityStack.size();
            if (i >= size) {
                return;
            }
            WeakReference<Activity> weakReference = activityStack.get(i);
            i++;
            if (weakReference == null || weakReference.get() == null) {
                i--;
                activityStack.remove(weakReference);
                continue;
            }
            if (weakReference.get().getClass().equals(cls)) {
                continue;
            }
            weakReference.get().finish();
            activityStack.remove(weakReference);
            i--;
        }
    }

    @RequiresPermission(value = Manifest.permission.KILL_BACKGROUND_PROCESSES)
    public void quitApp(Context context) {
        try {
            finishAllActivity();
        } catch (Exception e) {
        }

        // 获取packagemanager的实例
        try {
            android.app.ActivityManager activityMgr = (android.app.ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.killBackgroundProcesses(context.getPackageName());
            activityStack = null;
            instance = null;
        } catch (Exception e) {
        } finally {
            System.exit(0);
        }
    }

    /**
     * 添加activity
     *
     * @param activity
     */
    private void pushActivity(Activity activity) {
        Log.i("ActivityManager", "当前入栈activity ===>>>" + activity.getClass().getSimpleName());
        if (activityStack == null) {
            activityStack = new Stack<WeakReference<Activity>>();
        }
        WeakReference<Activity> item = findWeakByActivity(activity);
        if (item == null) {
            activityStack.add(new WeakReference<Activity>(activity));
        }
    }

    /**
     * 移除activity, 但是不销毁
     *
     * @param activity
     */
    private void removeActivity(Activity activity) {
        Log.i("ActivityManager", "当前出栈activity ===>>>" + activity.getClass().getSimpleName());
        for (WeakReference<Activity> weakReference : activityStack) {
            if (weakReference != null && weakReference.get() != null && weakReference.get() == activity) {
                activityStack.remove(weakReference);
                return;
            }
        }
    }

    private class InnerActivityLifecycle implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            pushActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            removeActivity(activity);
        }
    }
}