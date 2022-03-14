package com.hapi.ut;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by xx on 2017/9/21.
 */

public class ScreenUtil {

    /**
     * 获取屏幕尺寸与密度.
     *
     * @return mDisplayMetrics
     */
    public static DisplayMetrics getDisplayMetrics() {
        return Resources.getSystem().getDisplayMetrics();
    }

    /**
     * 获取屏幕尺寸
     *
     * @return 数组 0：宽度， 1：高度
     */
    public static int[] getScreenSize() {
        DisplayMetrics displayMetrics = getDisplayMetrics();
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }

    /**
     * 得到设备屏幕的宽度
     */
    public static int getScreenWidth() {
        return getScreenSize()[0];
    }

    /**
     * 得到设备屏幕的高度
     */
    public static int getScreenHeight() {
        return getScreenSize()[1];
    }

    /**
     * 得到设备的密度
     */
    public static float getScreenDensity() {
        return getDisplayMetrics().density;
    }

    /**
     * 获取屏幕真实DisplayMetrics（包含底部导航栏）
     *
     * @return
     */
    public static DisplayMetrics getRealDisplayMetrics() {
        WindowManager windowManager = (WindowManager) AppCache.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return AppCache.getContext().getResources().getDisplayMetrics();
        }
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    /**
     * 获取屏幕原始尺寸高度，包括虚拟功能键高度
     *
     * @return
     */
    public static int[] getRealScreenSize() {
        DisplayMetrics displayMetrics = getRealDisplayMetrics();
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }

    /**
     * 是否为横屏状态
     */
    public static boolean isHorScreen() {
        return Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean hasNavBar(@NonNull Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);
        }

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        boolean hasNavbar = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        Log.i("ScreenUtil", "hasNavbar:" + hasNavbar);
        return hasNavbar;
    }

    /**
     * 获取NavigationBar的高度，分为横竖屏不同状态
     */
    @Deprecated
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Activity activity = null;
            if (context instanceof Activity) {
                activity = (Activity) context;
            } else if (context instanceof ContextThemeWrapper) {
                Context baseContext = ((ContextThemeWrapper) context).getBaseContext();
                if (baseContext instanceof Activity) {
                    activity = (Activity) baseContext;
                }
            } else if (context instanceof android.view.ContextThemeWrapper) {
                Context baseContext = ((android.view.ContextThemeWrapper) context).getBaseContext();
                if (baseContext instanceof Activity) {
                    activity = (Activity) baseContext;
                }
            }
            if (activity != null && hasNavBar(activity)) {
                String key;
                if (isHorScreen()) {
                    key = "navigation_bar_height_landscape";
                } else {
                    key = "navigation_bar_height";
                }
                return getInternalDimensionSize(context.getResources(), key);
            }
        }
        return result;
    }

    /**
     * 获取NavigationBar的高度，分为横竖屏不同状态
     */
    public static int getNavigationBarHeight() {
        if (isHorScreen()) {
            int total = getRealScreenSize()[0];
            int cur = getScreenWidth();
            return total - cur;
        } else {
            int total = getRealScreenSize()[1];
            int cur = getScreenHeight();
            return total - cur;
        }
    }

    public static void showNavigation(Activity activity) {
        if (hasNavBar(activity)) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    public static void hideNavigation(Activity activity) {
        if (hasNavBar(activity)) {
            int uiOptions = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
            } else {
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context c) {
        return getInternalDimensionSize(c.getResources(), "status_bar_height");
    }

    /**
     * 获取安卓系统内部资源定义的dimens
     */
    public static int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }


    /**
     * 判断是否为全屏
     *
     * @param activity
     * @return
     */
    public static boolean isFullScreen(Activity activity) {
        int flags = activity.getWindow().getAttributes().flags;
        if ((flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 全屏切换
     */
    public static void fullScreenChange(Activity activity) {
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean fullScreen = mPreferences.getBoolean("fullScreen", false);
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        if (fullScreen) {
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attrs);
            // 取消全屏设置
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            mPreferences.edit().putBoolean("fullScreen", false).commit();
        } else {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(attrs);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            mPreferences.edit().putBoolean("fullScreen", true).commit();
        }
    }

    /**
     * 全屏切换
     */
    public static void fullScreenChange(Activity activity, boolean fullScreen, boolean inScreen) {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        if (fullScreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(attrs);
//            activity.getWindow().addFlags(inScreen ?
//                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//                    : WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attrs);
            // 取消全屏设置
//            activity.getWindow().clearFlags(inScreen ?
//                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//                    : WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
}