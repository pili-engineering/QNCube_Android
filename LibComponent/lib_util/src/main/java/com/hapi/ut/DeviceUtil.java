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

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.StatFs;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.hapi.ut.constans.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * 设备信息工具类
 *
 * @author cai
 */
@SuppressLint("MissingPermission")
public class DeviceUtil {

    public static final int MinSDCardSpace = 100; //最低内存剩余空间，100M

    /**
     * 判断当前手机是否有ROOT权限
     *
     * @return
     */
    public boolean isRoot() {
        boolean bool = false;

        try {
            if ((!new File("/system/bin/su").exists())
                    && (!new File("/system/xbin/su").exists())) {
                bool = false;
            } else {
                bool = true;
            }
        } catch (Exception e) {

        }
        return bool;
    }

    /**
     * 描述：SD卡是否能用.
     *
     * @return true 可用,false不可用
     */
    public static boolean isCanUseSD() {
        try {
            return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取SDCard根路径
     *
     * @return
     */
    public static String getSDPath() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();// 获取跟目录
        }
        return null;
    }

    /**
     * 判断SDCard剩余内存是否够，（是否大于100M）
     *
     * @return
     */
    public static boolean isSDCardSpaceEnough() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = 0;
        if (Build.VERSION.SDK_INT >= 18) {
            sdFreeMB = ((double) stat.getAvailableBlocksLong() * (double) stat.getBlockSizeLong()) / (1024 * 1024);
        } else {
            sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize()) / (1024 * 1024);
        }
        if (sdFreeMB >= MinSDCardSpace) {
            return true;
        }
        return false;
    }

    /**
     * 计算sdcard上的剩余空间
     */
    public static int getfreeSpaceOnSD() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = 0;
        if (Build.VERSION.SDK_INT >= 18) {
            sdFreeMB = ((double) stat.getAvailableBlocksLong() * (double) stat.getBlockSizeLong()) / Constants.MB;
        } else {
            sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize()) / Constants.MB;
        }
        return (int) sdFreeMB;
    }

    /**
     * 返回正确的UserAgent
     *
     * @return
     */
    public static String getUserAgent() {
        //Dalvik/2.1.0 (Linux; U; Android 6.0.1; vivo X9L Build/MMB29M)
        String userAgent = System.getProperty("http.agent");
        if (TextUtils.isEmpty(userAgent)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        Log.d("User-Agent", "User-Agent: " + sb.toString());
        return sb.toString();
    }

    /**
     * 获取cpu内核个数
     *
     * @return The number of cores, or 1 if failed to get result
     */
    @WorkerThread
    public static int getNumCores() {
        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    // Check if filename is "cpu", followed by a single digit
                    // number
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }

            });
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            // Default to return 1 core
            return 1;
        }
    }

    /**
     * 获取系统总内存
     *
     * @return 总内存大单位为B。
     */
    @WorkerThread
    public static long getTotalMemorySize() {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024L;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 通过pid获取应用占用的内存
     */
    public static int getPidMemorySize(int pid, Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int[] myMempid = new int[]{pid};
        Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(myMempid);
        memoryInfo[0].getTotalSharedDirty();
        int memSize = memoryInfo[0].getTotalPss();
        return memSize;
    }

    /**
     * 获取CPU型号
     *
     * @return
     */
    @WorkerThread
    public static String getCpuName() {
        String str1 = "/proc/cpuinfo";
        String str2 = "";

        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr);
            while ((str2 = localBufferedReader.readLine()) != null) {
                if (str2.contains("Hardware")) {
                    return str2.split(":")[1];
                }
            }
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return null;

    }

    /**
     * 获取CPU频率
     *
     * @return
     */
    @WorkerThread
    public static float getProcessCpuRate() {
        float totalCpuTime1 = getTotalCpuTime();
        float processCpuTime1 = getAppCpuTime();
        try {
            Thread.sleep(360);
        } catch (Exception e) {
        }

        float totalCpuTime2 = getTotalCpuTime();
        float processCpuTime2 = getAppCpuTime();

        float cpuRate = 100 * (processCpuTime2 - processCpuTime1)
                / (totalCpuTime2 - totalCpuTime1);

        return cpuRate;
    }

    /**
     * 获取系统总CPU使用时间
     *
     * @return
     */
    @WorkerThread
    public static long getTotalCpuTime() {
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long totalCpu = Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return totalCpu;
    }

    /**
     * 获取应用占用的CPU时间
     *
     * @return
     */
    @WorkerThread
    public static long getAppCpuTime() {
        String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }

    /**
     * 检测设备是否有陀螺仪属性
     *
     * @return
     */
    public static boolean checkDeviceGyroscope(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm != null && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
    }
}
