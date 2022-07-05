package com.hapi.base_mvvm.refresh;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *
 * @author blabla
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
            @SuppressLint("MissingPermission") NetworkInfo networkInfo = manager.getActiveNetworkInfo();
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
    @SuppressLint("MissingPermission")
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }


}