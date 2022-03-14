package com.hapi.ut;

import java.math.BigDecimal;

/**
 * Created by xx on 2017/6/15.
 */

public class MathUtil {

    private static final int BASE_OVER_LENGTH = 1_000;
    private static final int THOUSAND_OVER_LENGTH = BASE_OVER_LENGTH;
    private static final int TEN_THOUSAND_OVER_LENGTH = 10 * BASE_OVER_LENGTH;

    public static String transformPropNumberToString(int number) {
        if (number >= THOUSAND_OVER_LENGTH) {
            return number / 1000 + "." + (number % 1000) / 100 + "K";
        }
        return number + "";
    }

    /**
     * 以千为单位
     * 保留一个小数点
     *
     * @param number
     * @return
     */
    public static String formatToK(long number) {
        if (number > THOUSAND_OVER_LENGTH) {
            BigDecimal b1 = new BigDecimal(number);
            BigDecimal b2 = new BigDecimal(THOUSAND_OVER_LENGTH);
            return b1.divide(b2, 1, BigDecimal.ROUND_HALF_UP).doubleValue() + "K";
        }
        return number + "";
    }

    /**
     * 以万为单位
     * 保留两个小数点
     *
     * @param number
     * @param scale  保留几位小数
     * @return
     */
    public static String formatToW(long number, int scale) {
        if (number > TEN_THOUSAND_OVER_LENGTH) {
            BigDecimal b1 = new BigDecimal(number);
            BigDecimal b2 = new BigDecimal(TEN_THOUSAND_OVER_LENGTH);
            if (scale == 0) {
                return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).intValue() + "W";
            }
            return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue() + "W";
        }
        return number + "";
    }

    /**
     * 以万为单位
     * 保留一个小数点
     * 假如是.0w，则直接显示w
     *
     * @param number
     * @return
     */
    public static String formatToWString(long number) {
        if (number > TEN_THOUSAND_OVER_LENGTH) {
            BigDecimal b1 = new BigDecimal(number);
            BigDecimal b2 = new BigDecimal(TEN_THOUSAND_OVER_LENGTH);
            String result = b1.divide(b2, 1, BigDecimal.ROUND_HALF_UP).doubleValue() + "w";
            return result.replace(".0", "");
        }
        return number + "";
    }

    /**
     * 以万为单位 向下取整
     * 保留一个小数点
     * 假如是.0w，则直接显示w
     *
     * @param number
     * @return
     */
    public static String formatToWDownString(long number) {
        if (number > TEN_THOUSAND_OVER_LENGTH) {
            BigDecimal b1 = new BigDecimal(number);
            BigDecimal b2 = new BigDecimal(TEN_THOUSAND_OVER_LENGTH);
            String result = b1.divide(b2, 1, BigDecimal.ROUND_DOWN).doubleValue() + "w";
            return result.replace(".0", "");
        }
        return number + "";
    }

    public static int getIntegerValue(Integer integer) {
        if (integer == null) {
            return 0;
        }
        return integer.intValue();
    }

    public static String getIntegerStr(Integer integer) {
        if (integer == null) {
            return "0";
        }
        return integer.toString();
    }
}
