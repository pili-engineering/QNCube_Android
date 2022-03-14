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

import android.os.Build;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 操作系统工具类
 * Created by xx on 2016/11/5.
 */

public class OsInfoUtil {
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    public static boolean isMIUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isFlyme() {
        try {
            // Invoke Build.hasSmartBar()
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * {@link Build.VERSION_CODES#JELLY_BEAN}
     *
     * @return
     */
    public static boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * {@link Build.VERSION_CODES#KITKAT}
     *
     * @return
     */
    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * {@link Build.VERSION_CODES#LOLLIPOP}
     *
     * @return
     */
    public static boolean isLOLLIPOP() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * {@link Build.VERSION_CODES#M}
     *
     * @return
     */
    public static boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * {@link Build.VERSION_CODES#N}
     *
     * @return
     */
    public static boolean isN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean OverSDK16() {
        return Build.VERSION.SDK_INT >= 16;
    }

    public static boolean OverSDK21() {
        return Build.VERSION.SDK_INT >= 21;
    }

    public static String getPhoneModel() {
        return Build.BRAND + " " + Build.MODEL;
    }

    public static boolean isSamsungPhone() {
        if (getPhoneModel().contains("samsung")) {
            return true;
        }
        return false;
    }

    public static boolean isHuaweiPhone() {
        if (getPhoneModel().contains("huawei") || getPhoneModel().contains("honour")) {
            return true;
        }
        return false;
    }
}
