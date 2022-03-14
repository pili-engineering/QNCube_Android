package com.hapi.ut;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 从TelephonyManager读取的信息<br>
 *
 * @author xx
 * @date 2018/11/13
 */
public class PhoneUtil {

    public static final String DeviceId = "DeviceId";
    public static final String DeviceSoftwareVersion = "DeviceSoftwareVersion";
    public static final String Line1Number = "Line1Number";
    public static final String NetworkCountryIso = "NetworkCountryIso";
    public static final String NetworkOperator = "NetworkOperator";
    public static final String NetworkOperatorName = "NetworkOperatorName";
    public static final String NetworkType = "NetworkType";
    public static final String PhoneType = "PhoneType";
    public static final String SimCountryIso = "SimCountryIso";
    public static final String SimOperator = "SimOperator";
    public static final String SimOperatorName = "SimOperatorName";
    public static final String SimSerialNumber = "SimSerialNumber";
    public static final String SimState = "SimState";
    // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
    public static final String SubscriberId = "SubscriberId";
    public static final String VoiceMailNumber = "VoiceMailNumber";

    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    public static Map<String, Object> readDevice(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DeviceId, tm.getDeviceId());
        map.put(DeviceSoftwareVersion, tm.getDeviceSoftwareVersion());
        map.put(Line1Number, tm.getLine1Number());
        map.put(NetworkCountryIso, tm.getNetworkCountryIso());
        map.put(NetworkOperator, tm.getNetworkOperator());
        map.put(NetworkOperatorName, tm.getNetworkOperatorName());
        map.put(NetworkType, tm.getNetworkType());
        map.put(PhoneType, tm.getPhoneType());
        map.put(SimCountryIso, tm.getSimCountryIso());
        map.put(SimOperator, tm.getSimOperator());
        map.put(SimOperatorName, tm.getSimOperatorName());
        map.put(SimSerialNumber, tm.getSimSerialNumber());
        map.put(SimState, tm.getSimState());
        map.put(SubscriberId, tm.getSubscriberId());
        map.put(VoiceMailNumber, tm.getVoiceMailNumber());
        return map;
    }

    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    public static String readSIMCard(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);// 取得相关系统服务
        StringBuffer sb = new StringBuffer();
        switch (tm.getSimState()) { // getSimState()取得sim的状态 有下面6中状态
            case TelephonyManager.SIM_STATE_ABSENT:
                sb.append("无卡");
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                sb.append("未知状态");
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                sb.append("需要NetworkPIN解锁");
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                sb.append("需要PIN解锁");
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                sb.append("需要PUK解锁");
                break;
            case TelephonyManager.SIM_STATE_READY:
                sb.append("良好");
                break;
        }

        if (tm.getSimSerialNumber() != null) {
            sb.append("@" + tm.getSimSerialNumber().toString());
        } else {
            sb.append("@无法取得SIM卡号");
        }

        if (tm.getSimOperator().equals("")) {
            sb.append("@无法取得供货商代码");
        } else {
            sb.append("@" + tm.getSimOperator().toString());
        }

        if (tm.getSimOperatorName().equals("")) {
            sb.append("@无法取得供货商");
        } else {
            sb.append("@" + tm.getSimOperatorName().toString());
        }

        if (tm.getSimCountryIso().equals("")) {
            sb.append("@无法取得国籍");
        } else {
            sb.append("@" + tm.getSimCountryIso().toString());
        }

        if (tm.getNetworkOperator().equals("")) {
            sb.append("@无法取得网络运营商");
        } else {
            sb.append("@" + tm.getNetworkOperator());
        }
        if (tm.getNetworkOperatorName().equals("")) {
            sb.append("@无法取得网络运营商名称");
        } else {
            sb.append("@" + tm.getNetworkOperatorName());
        }
        if (tm.getNetworkType() == 0) {
            sb.append("@无法取得网络类型");
        } else {
            sb.append("@" + tm.getNetworkType());
        }
        return sb.toString();
    }

    /* 获取Sim卡序列号 */
    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    public static String getSimSerial(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return null;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return tm.getSimSerialNumber();
    }

    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    public static String getPhoneNo(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        switch (tm.getSimState()) { // getSimState()取得sim的状态 有下面6中状态
            case TelephonyManager.SIM_STATE_READY:
                return tm.getLine1Number();
            case TelephonyManager.SIM_STATE_ABSENT:
            case TelephonyManager.SIM_STATE_UNKNOWN:
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
            default:
                return null;
        }
    }

    /**
     * 获取手机信息
     *
     * @return
     */
    public static String getMobileInfo() {
        StringBuffer sb = new StringBuffer();
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                sb.append(name + "=" + value);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    @Deprecated
    public static String getDeviceIdByTel(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    public static String getDeviceIdSignature(Context context, String slat, String defaultSign) {
        String id = PhoneUtil.getAndroidId(context) + slat;
        String digest = EncryptUtil.MD5(id);
        return (null == digest) ? defaultSign : digest.toLowerCase();
    }

    public static String getDeviceIdSignature(String id, String slat, String defaultSign) {
        String tmp = id + slat;
        String digest = EncryptUtil.MD5(tmp);
        return (null == digest) ? defaultSign : digest.toLowerCase();
    }
}
