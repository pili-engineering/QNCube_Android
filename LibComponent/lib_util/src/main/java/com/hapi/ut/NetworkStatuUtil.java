//package com.pince.ut;
//
//import android.content.Context;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.telephony.TelephonyManager;
//
//import static android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_IDEN;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;
//import static android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN;
//
//public class NetworkStatuUtil {
//    private static final int NETWORK_TYPE_UNAVAILABLE = -1;
//    // private static final int NETWORK_TYPE_MOBILE = -100;
//    private static final int NETWORK_TYPE_WIFI = -101;
//
//    private static final int NETWORK_CLASS_WIFI = -101;
//    private static final int NETWORK_CLASS_UNAVAILABLE = -1;
//    /**
//     * Unknown network class.
//     */
//    private static final int NETWORK_CLASS_UNKNOWN = 0;
//    /**
//     * Class of broadly defined "2G" networks.
//     */
//    private static final int NETWORK_CLASS_2_G = 1;
//    /**
//     * Class of broadly defined "3G" networks.
//     */
//    private static final int NETWORK_CLASS_3_G = 2;
//    /**
//     * Class of broadly defined "4G" networks.
//     */
//    private static final int NETWORK_CLASS_4_G = 3;
//
//    private static int getNetworkClass() {
//        int networkType = NETWORK_TYPE_UNKNOWN;
//        try {
//            final NetworkInfo network = ((ConnectivityManager) AppCache.getContext()
//                    .getSystemService(Context.CONNECTIVITY_SERVICE))
//                    .getActiveNetworkInfo();
//            if (network != null && network.isAvailable()
//                    && network.isConnected()) {
//                int type = network.getType();
//                if (type == ConnectivityManager.TYPE_WIFI) {
//                    networkType = NETWORK_TYPE_WIFI;
//                } else if (type == ConnectivityManager.TYPE_MOBILE) {
//                    TelephonyManager telephonyManager = (TelephonyManager) AppCache
//                            .getContext().getSystemService(
//                                    Context.TELEPHONY_SERVICE);
//                    networkType = telephonyManager.getNetworkType();
//                }
//            } else {
//                networkType = NETWORK_TYPE_UNAVAILABLE;
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return getNetworkClassByType(networkType);
//
//    }
//
//    private static int getNetworkClassByType(int networkType) {
//        switch (networkType) {
//            case NETWORK_TYPE_UNAVAILABLE:
//                return NETWORK_CLASS_UNAVAILABLE;
//            case NETWORK_TYPE_WIFI:
//                return NETWORK_CLASS_WIFI;
//            case NETWORK_TYPE_GPRS:
//            case NETWORK_TYPE_EDGE:
//            case NETWORK_TYPE_CDMA:
//            case NETWORK_TYPE_1xRTT:
//            case NETWORK_TYPE_IDEN:
//                return NETWORK_CLASS_2_G;
//            case NETWORK_TYPE_UMTS:
//            case NETWORK_TYPE_EVDO_0:
//            case NETWORK_TYPE_EVDO_A:
//            case NETWORK_TYPE_HSDPA:
//            case NETWORK_TYPE_HSUPA:
//            case NETWORK_TYPE_HSPA:
//            case NETWORK_TYPE_EVDO_B:
//            case NETWORK_TYPE_EHRPD:
//            case NETWORK_TYPE_HSPAP:
//                return NETWORK_CLASS_3_G;
//            case NETWORK_TYPE_LTE:
//                return NETWORK_CLASS_4_G;
//            default:
//                return NETWORK_CLASS_UNKNOWN;
//        }
//    }
//
//    public static int getCurrentNetworkType() {
//        int networkClass = getNetworkClass();
//        String type = "未知";
//        int code = -1;
//        switch (networkClass) {
//            case NETWORK_CLASS_UNAVAILABLE:
//                type = "无";
//                code = 0;
//                break;
//            case NETWORK_CLASS_WIFI:
//                type = "Wi-Fi";
//                code = 1;
//                break;
//            case NETWORK_CLASS_2_G:
//                type = "2G";
//                code = 2;
//                break;
//            case NETWORK_CLASS_3_G:
//                type = "3G";
//                code = 3;
//                break;
//            case NETWORK_CLASS_4_G:
//                type = "4G";
//                code = 4;
//                break;
//            case NETWORK_CLASS_UNKNOWN:
//                type = "未知";
//                code = -1;
//                break;
//        }
//        return code;
//    }
//}
