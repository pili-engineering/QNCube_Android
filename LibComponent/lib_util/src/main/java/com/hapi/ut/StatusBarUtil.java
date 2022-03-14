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

package com.hapi.ut;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 状态栏工具类
 * @author xx
 * @date 16/2/14
 */
public class StatusBarUtil {

    public static final int DEFAULT_STATUS_BAR_ALPHA = 0;

    /**
     * 设置状态栏字体颜色是否加深
     *
     * @param activity
     * @param isDark   true为加深
     * @return 1:MIUUI 2:Flyme 3:android6.0
     */
    public static int setStatusBarTextColor(Activity activity, boolean isDark) {
        if (OsInfoUtil.isMIUI()) {
            return setMiuiStatusBarDarkMode(activity, isDark);
        } else if (OsInfoUtil.isFlyme()) {
            return setMeizuStatusBarDarkIcon(activity, isDark);
        } else {
            return setStatusBarLightMode(activity, isDark);
        }
    }

    /**
     * set status bar translucency
     */
    public static void setTranslucentStatus(Activity activity, boolean on) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * set status bar translucency
     */
    public static void setTranslucentStatusPadding(Activity activity, boolean on) {
        if (OsInfoUtil.isKitkat()) {
            Window win = activity.getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                StatusBarUtil.setRootView(activity);
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
    }

    /**
     * 设置6.0+状态栏字体颜色
     *
     * @param activity
     * @param isFontColorDark
     */
    private static int setStatusBarLightMode(Activity activity, boolean isFontColorDark) {
        if (OsInfoUtil.isM()) {
            if (isFontColorDark) {
                // 沉浸式
                //                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                //非沉浸式
//                activity.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else {
                //非沉浸式
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            return 3;
        }
        return 0;
    }

    /**
     * MIUI 状态栏字体颜色
     *
     * @param activity
     * @param darkmode
     * @return
     */
    private static int setMiuiStatusBarDarkMode(Activity activity,
                                                boolean darkmode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class
                    .forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams
                    .getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Window window = activity.getWindow();
//        Class clazz = window.getClass();
//        try {
//            int tranceFlag = 0;
//            int darkModeFlag = 0;
//            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
//            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_TRANSPARENT");
//            tranceFlag = field.getInt(layoutParams);
//            field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
//            darkModeFlag = field.getInt(layoutParams);
//            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
//            //只需要状态栏透明
//            extraFlagField.invoke(window, tranceFlag, tranceFlag);
//            //状态栏透明且黑色字体
//            extraFlagField.invoke(window, tranceFlag | darkModeFlag, tranceFlag | darkModeFlag);
//            //清除黑色字体
//            extraFlagField.invoke(window, 0, darkModeFlag);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
        return 0;
    }

    /**
     * flyme状态栏字体颜色
     *
     * @param activity
     * @param dark
     * @return
     */
    private static int setMeizuStatusBarDarkIcon(Activity activity, boolean dark) {
        if (activity != null) {
            try {
                WindowManager.LayoutParams lp = activity.getWindow()
                        .getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                activity.getWindow().setAttributes(lp);
                return 2;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


    /**
     * 颜色加深处理
     *
     * @param RGBValues RGB的值，由alpha（透明度）、red（红）、green（绿）、blue（蓝）构成，
     *                  Android中我们一般使用它的16进制，
     *                  例如："#FFAABBCC",最左边到最右每两个字母就是代表alpha（透明度）、
     *                  red（红）、green（绿）、blue（蓝）。每种颜色值占一个字节(8位)，值域0~255
     *                  所以下面使用移位的方法可以得到每种颜色的值，然后每种颜色值减小一下，在合成RGB颜色，颜色就会看起来深一些了
     * @return
     */
    private int colorBurn(int RGBValues) {
        int alpha = RGBValues >> 24;
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 - 0.1));
        green = (int) Math.floor(green * (1 - 0.1));
        blue = (int) Math.floor(blue * (1 - 0.1));
        return Color.rgb(red, green, blue);
    }

    /**
     * 设置状态栏颜色
     *
     * @param activity 需要设置的 activity
     * @param color    状态栏颜色值
     */
    public static void setColor(Activity activity, int color) {
        setColor(activity, color, DEFAULT_STATUS_BAR_ALPHA);
    }

    /**
     * 设置状态栏颜色
     *
     * @param activity       需要设置的activity
     * @param color          状态栏颜色值
     * @param statusBarAlpha 状态栏透明度 0x00 - 0xff
     */
    public static void setColor(Activity activity, int color, int statusBarAlpha) {
        if (OsInfoUtil.isLOLLIPOP()) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(calculateStatusColor(color, statusBarAlpha));
        } else if (OsInfoUtil.isKitkat()) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 生成一个状态栏大小的矩形
            View statusView = createStatusBarView(activity, color, statusBarAlpha);
            // 添加 statusView 到布局中
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(statusView);
            setRootView(activity);
        }
    }

    /**
     * 设置状态栏纯色 不加半透明效果
     *
     * @param activity 需要设置的 activity
     * @param color    状态栏颜色值
     */
    public static void setColorNoTranslucent(Activity activity, int color) {
        setColor(activity, color, 0);
    }

    /**
     * 设置状态栏颜色(5.0以下无半透明效果,不建议使用)
     *
     * @param activity 需要设置的 activity
     * @param color    状态栏颜色值
     */
    public static void setColorDiff(Activity activity, int color) {
        if (OsInfoUtil.isKitkat()) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 生成一个状态栏大小的矩形
            View statusView = createStatusBarView(activity, color);
            // 添加 statusView 到布局中
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(statusView);
            setRootView(activity);
        }
    }

    /**
     * 使状态栏半透明
     * <p/>
     * 适用于图片作为背景的界面,此时需要图片填充到状态栏
     *
     * @param activity 需要设置的activity
     */
    public static void setTranslucent(Activity activity) {
        setTranslucent(activity, DEFAULT_STATUS_BAR_ALPHA);
    }

    /**
     * 使状态栏半透明
     * <p/>
     * 适用于图片作为背景的界面,此时需要图片填充到状态栏
     *
     * @param activity       需要设置的activity
     * @param statusBarAlpha 状态栏透明度
     */
    public static void setTranslucent(Activity activity, int statusBarAlpha) {
        if (OsInfoUtil.isKitkat()) {
            setTransparent(activity);
            addTranslucentView(activity, statusBarAlpha);
        }
    }

    /**
     * 设置状态栏全透明
     *
     * @param activity 需要设置的activity
     */
    public static void setTransparent(Activity activity) {
        if (OsInfoUtil.isKitkat()) {
            transparentStatusBar(activity);
            setRootView(activity);
        }
    }

    /**
     * 使状态栏透明(5.0以上半透明效果,不建议使用)
     * <p/>
     * 适用于图片作为背景的界面,此时需要图片填充到状态栏
     *
     * @param activity 需要设置的activity
     */
    public static void setTranslucentDiff(Activity activity) {
        if (OsInfoUtil.isKitkat()) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setRootView(activity);
        }
    }

    /**
     * 为DrawerLayout 布局设置状态栏变色
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     * @param color        状态栏颜色值
     */
    public static void setColorForDrawerLayout(Activity activity, DrawerLayout drawerLayout,
                                               int color) {
        setColorForDrawerLayout(activity, drawerLayout, color, DEFAULT_STATUS_BAR_ALPHA);
    }

    /**
     * 为DrawerLayout 布局设置状态栏颜色,纯色
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     * @param color        状态栏颜色值
     */
    public static void setColorNoTranslucentForDrawerLayout(Activity activity,
                                                            DrawerLayout drawerLayout, int color) {
        setColorForDrawerLayout(activity, drawerLayout, color, 0);
    }

    /**
     * 为DrawerLayout 布局设置状态栏变色
     *
     * @param activity       需要设置的activity
     * @param drawerLayout   DrawerLayout
     * @param color          状态栏颜色值
     * @param statusBarAlpha 状态栏透明度
     */
    public static void setColorForDrawerLayout(Activity activity, DrawerLayout drawerLayout,
                                               int color, int statusBarAlpha) {
        if (OsInfoUtil.isKitkat()) {
            if (OsInfoUtil.isLOLLIPOP()) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            // 生成一个状态栏大小的矩形
            View statusBarView = createStatusBarView(activity, color);
            // 添加 statusBarView 到布局中
            ViewGroup contentLayout = (ViewGroup) drawerLayout.getChildAt(0);
            contentLayout.addView(statusBarView, 0);
            // 内容布局不是 LinearLayout 时,设置padding top
            if (!(contentLayout instanceof LinearLayout) && contentLayout.getChildAt(1) != null) {
                contentLayout.getChildAt(1).setPadding(0, getStatusBarHeight(activity), 0, 0);
            }
            // 设置属性
            ViewGroup drawer = (ViewGroup) drawerLayout.getChildAt(1);
            drawerLayout.setFitsSystemWindows(false);
            contentLayout.setFitsSystemWindows(false);
            contentLayout.setClipToPadding(true);
            drawer.setFitsSystemWindows(false);

            addTranslucentView(activity, statusBarAlpha);
        }
    }

    /**
     * 为DrawerLayout 布局设置状态栏变色(5.0以下无半透明效果,不建议使用)
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     * @param color        状态栏颜色值
     */
    public static void setColorForDrawerLayoutDiff(Activity activity, DrawerLayout drawerLayout, int color) {
        if (OsInfoUtil.isKitkat()) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 生成一个状态栏大小的矩形
            View statusBarView = createStatusBarView(activity, color);
            // 添加 statusBarView 到布局中
            ViewGroup contentLayout = (ViewGroup) drawerLayout.getChildAt(0);
            contentLayout.addView(statusBarView, 0);
            // 内容布局不是 LinearLayout 时,设置padding top
            if (!(contentLayout instanceof LinearLayout) && contentLayout.getChildAt(1) != null) {
                contentLayout.getChildAt(1).setPadding(0, getStatusBarHeight(activity), 0, 0);
            }
            // 设置属性
            ViewGroup drawer = (ViewGroup) drawerLayout.getChildAt(1);
            drawerLayout.setFitsSystemWindows(false);
            contentLayout.setFitsSystemWindows(false);
            contentLayout.setClipToPadding(true);
            drawer.setFitsSystemWindows(false);
        }
    }

    /**
     * 为 DrawerLayout 布局设置状态栏透明
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     */
    public static void setTranslucentForDrawerLayout(Activity activity, DrawerLayout drawerLayout) {
        setTranslucentForDrawerLayout(activity, drawerLayout, DEFAULT_STATUS_BAR_ALPHA);
    }

    /**
     * 为 DrawerLayout 布局设置状态栏透明
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     */
    public static void setTranslucentForDrawerLayout(Activity activity, DrawerLayout drawerLayout, int statusBarAlpha) {
        if (OsInfoUtil.isKitkat()) {
            setTransparentForDrawerLayout(activity, drawerLayout);
            addTranslucentView(activity, statusBarAlpha);
        }
    }

    /**
     * 为 DrawerLayout 布局设置状态栏透明
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     */
    public static void setTransparentForDrawerLayout(Activity activity, DrawerLayout drawerLayout) {
        if (OsInfoUtil.isKitkat()) {
            if (OsInfoUtil.isLOLLIPOP()) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }

            ViewGroup contentLayout = (ViewGroup) drawerLayout.getChildAt(0);
            // 内容布局不是 LinearLayout 时,设置padding top
            if (!(contentLayout instanceof LinearLayout) && contentLayout.getChildAt(1) != null) {
                contentLayout.getChildAt(1).setPadding(0, getStatusBarHeight(activity), 0, 0);
            }

            // 设置属性
            ViewGroup drawer = (ViewGroup) drawerLayout.getChildAt(1);
            drawerLayout.setFitsSystemWindows(false);
            contentLayout.setFitsSystemWindows(false);
            contentLayout.setClipToPadding(true);
            drawer.setFitsSystemWindows(false);
        }
    }

    /**
     * 为 DrawerLayout 布局设置状态栏透明(5.0以上半透明效果,不建议使用)
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     */
    public static void setTranslucentForDrawerLayoutDiff(Activity activity, DrawerLayout drawerLayout) {
        if (OsInfoUtil.isKitkat()) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 设置内容布局属性
            ViewGroup contentLayout = (ViewGroup) drawerLayout.getChildAt(0);
            contentLayout.setFitsSystemWindows(true);
            contentLayout.setClipToPadding(true);
            // 设置抽屉布局属性
            ViewGroup vg = (ViewGroup) drawerLayout.getChildAt(1);
            vg.setFitsSystemWindows(false);
            // 设置 DrawerLayout 属性
            drawerLayout.setFitsSystemWindows(false);
        }
    }

    /**
     * 添加半透明矩形条
     *
     * @param activity       需要设置的 activity
     * @param statusBarAlpha 透明值
     */
    private static void addTranslucentView(Activity activity, int statusBarAlpha) {
        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        // 移除半透明矩形,以免叠加
        if (contentView.getChildCount() > 1) {
            contentView.removeViewAt(1);
        }
        contentView.addView(createTranslucentStatusBarView(activity, statusBarAlpha));
    }

    /**
     * 生成一个和状态栏大小相同的彩色矩形条
     *
     * @param activity 需要设置的 activity
     * @param color    状态栏颜色值
     * @return 状态栏矩形条
     */
    private static View createStatusBarView(Activity activity, int color) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(color);
        return statusBarView;
    }

    /**
     * 生成一个和状态栏大小相同的半透明矩形条
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     * @param alpha    透明值
     * @return 状态栏矩形条
     */
    private static View createStatusBarView(Activity activity, int color, int alpha) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(calculateStatusColor(color, alpha));
        return statusBarView;
    }

    /**
     * 设置根布局参数
     */
    public static void setRootView(Activity activity) {
        ViewGroup rootView =
                (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        rootView.setFitsSystemWindows(true);
        rootView.setClipToPadding(true);
    }

    /**
     * 使状态栏透明
     */
    public static void transparentStatusBar(Activity activity) {
        if (OsInfoUtil.isLOLLIPOP()) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 创建半透明矩形 View
     *
     * @param alpha 透明值
     * @return 半透明 View
     */
    private static View createTranslucentStatusBarView(Activity activity, int alpha) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        return statusBarView;
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        // 获得状态栏高度
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    /**
     * 计算状态栏颜色
     *
     * @param color color值
     * @param alpha alpha值 0x00 - 0xff
     * @return 最终的状态栏颜色
     */
    private static int calculateStatusColor(int color, int alpha) {
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }

}
