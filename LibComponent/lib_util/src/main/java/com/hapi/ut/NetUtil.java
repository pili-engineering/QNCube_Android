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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import androidx.core.app.ActivityCompat;

/**
 *
 * @author xx
 * @date 2016/10/13
 */
public class NetUtil {
    /**
     * 检查是否有网络
     */
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    /**
     * 获取当前网络类型
     * @param context
     * @return
     */
    public static String getNetworkType(Context context) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            int type = networkInfo.getType();
            String typeName = networkInfo.getTypeName();
            String extraInfo = networkInfo.getExtraInfo();
            if (type == ConnectivityManager.TYPE_WIFI) {
                return typeName;
            } else {
                return extraInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 判断当前网络是否是wifi网络.
     *
     * @param context the context
     * @return boolean
     */
    public static boolean isWifiOpen(Context context) {
        NetworkInfo activeNetInfo = getNetworkInfo(context);
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 检查是否连接上WIFI （不一定联网）
     */
    public static boolean isWifiConnect(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI && info.isConnected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断wifi是否可用.
     *
     * @param context the context
     * @return true, if is wifi enabled
     */
    public static boolean isWifiEnabled(Context context) {
        NetworkInfo activeNetInfo = getNetworkInfo(context);
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI && activeNetInfo.isAvailable()) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前网络是否是3G/2G移动网络.
     *
     * @param context the context
     * @return boolean
     */
    public static boolean isMobile(Context context) {
        NetworkInfo activeNetInfo = getNetworkInfo(context);
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    /**
     *获取当前网络信息.
     *
     * @param context the context
     * @return NetworkInfo
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    /* 获取wifi地址 */
    public static String getWifiAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi==null){
            return null;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return wifi.getConnectionInfo().getBSSID();
    }
}