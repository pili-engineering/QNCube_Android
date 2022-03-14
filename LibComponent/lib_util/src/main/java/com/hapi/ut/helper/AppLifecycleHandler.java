package com.hapi.ut.helper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * app前后台监听
 * Created by xx on 16/5/18.
 */
public class AppLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "AppLifecycleHandler";

    private interface Callback {
        void invoke();

        final class Invoker {
            private final Callback callback;

            private Invoker(@Nullable Callback callback) {
                this.callback = callback;
            }

            private void invoke() {
                invoke(callback);
            }

            static void invoke(Callback callback) {
                if (null == callback) {
                    return;
                }
                callback.invoke();
            }
        }
    }

    public interface OnAppVisibleCallback extends Callback {
    }

    public interface OnAppInvisibleCallback extends Callback {
    }

    public interface OnAppForegroundCallback extends Callback {
    }

    public interface OnAppBackgroundCallback extends Callback {
    }

    private static final AppLifecycleHandler sInstance = new AppLifecycleHandler();

    private AppLifecycleHandler() {
        // Disabled.
    }

    public static AppLifecycleHandler getInstance() {
        return sInstance;
    }

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(getInstance());
    }

    private int mActivityStarted = 0;
    private int mActivityStopped = 0;
    private int mActivityResumed = 0;
    private int mActivityPaused = 0;

    private Set<OnAppVisibleCallback> mOnAppVisibleCallbacks = new HashSet<>();
    private Set<OnAppInvisibleCallback> mOnAppInvisibleCallbacks = new HashSet<>();
    private Set<OnAppForegroundCallback> mOnAppForegroundCallbacks = new HashSet<>();
    private Set<OnAppBackgroundCallback> mOnAppBackgroundCallbacks = new HashSet<>();

    synchronized public void addOnAppVisibleCallback(
            @NonNull final OnAppVisibleCallback callback) {
        mOnAppVisibleCallbacks.add(callback);
    }

    synchronized public void removeOnAppVisibleCallback(
            @NonNull final OnAppVisibleCallback callback) {
        mOnAppVisibleCallbacks.remove(callback);
    }

    synchronized public void addOnAppInvisibleCallback(
            @NonNull final OnAppInvisibleCallback callback) {
        mOnAppInvisibleCallbacks.add(callback);
    }

    synchronized public void removeOnAppInvisibleCallback(
            @NonNull final OnAppInvisibleCallback callback) {
        mOnAppInvisibleCallbacks.remove(callback);
    }

    synchronized public void addOnAppForegroundCallback(
            @NonNull final OnAppForegroundCallback callback) {
        mOnAppForegroundCallbacks.add(callback);
    }

    synchronized public void removeOnAppForegroundCallback(
            @NonNull final OnAppForegroundCallback callback) {
        mOnAppForegroundCallbacks.remove(callback);
    }

    synchronized public void addOnAppBackgroundCallback(
            @NonNull final OnAppBackgroundCallback callback) {
        mOnAppBackgroundCallbacks.add(callback);
    }

    synchronized public void removeOnAppBackgroundCallback(
            @NonNull final OnAppBackgroundCallback callback) {
        mOnAppBackgroundCallbacks.remove(callback);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activities.add(activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activities.remove(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        final boolean visible = isAppVisible();
        ++mActivityStarted;
        if (!visible && isAppVisible()) {
            onAppVisible();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        final boolean invisible = isAppInvisible();
        ++mActivityStopped;
        if (!invisible && isAppInvisible()) {
            onAppInvisible();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        final boolean foreground = isAppForeground();
        ++mActivityResumed;
        if (!foreground && isAppForeground()) {
            onAppForeground();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        final boolean background = isAppBackground();
        ++mActivityPaused;
        if (!background && isAppBackground()) {
            onAppBackground();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public boolean isAppVisible() {
        Log.d(TAG, ", [isAppVisible]: "
                + ", mActivityStarted: " + mActivityStarted
                + ", mActivityStopped: " + mActivityStopped
        );
        return mActivityStarted > mActivityStopped;
    }

    public boolean isAppInvisible() {
        Log.d(TAG, ", [isAppInvisible]: "
                + ", mActivityStarted: " + mActivityStarted
                + ", mActivityStopped: " + mActivityStopped);

        return !isAppVisible();
    }

    public boolean isAppForeground() {
        Log.d(TAG, ", [isAppForeground]: "
                + ", mActivityResumed: " + mActivityResumed +
                ", mActivityPaused: " + mActivityPaused);

        return mActivityResumed > mActivityPaused;
    }

    public boolean isAppBackground() {
        Log.d(TAG, ", [isAppBackground]: "
                + ", mActivityResumed: " + mActivityResumed
                + ", mActivityPaused: " + mActivityPaused);

        return !isAppForeground();
    }

    synchronized private void onAppVisible() {
        for (final OnAppVisibleCallback callback : mOnAppVisibleCallbacks) {
            Callback.Invoker.invoke(callback);
        }
    }

    synchronized private void onAppInvisible() {
        for (final OnAppInvisibleCallback callback : mOnAppInvisibleCallbacks) {
            Callback.Invoker.invoke(callback);
        }
    }

    synchronized private void onAppForeground() {
        for (final OnAppForegroundCallback callback : mOnAppForegroundCallbacks) {
            Callback.Invoker.invoke(callback);
        }
    }

    synchronized private void onAppBackground() {
        for (final OnAppBackgroundCallback callback : mOnAppBackgroundCallbacks) {
            Callback.Invoker.invoke(callback);
        }
    }

    private List<Activity> activities = new ArrayList<>();

    public List<Activity> getActiveActivities() {
        return activities;
    }

    public Activity getTopActivity() {
        if (activities.isEmpty()) {
            return null;
        }
        return activities.get(activities.size() - 1);
    }
}
