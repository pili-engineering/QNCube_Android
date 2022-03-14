package com.hapi.ut;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述：字符串处理类.
 *
 * @author xx
 * @version v1.0
 * @date：2014-1-17
 */
public final class StrUtil {

    private static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 描述：将null转化为“”.
     *
     * @param str 指定的字符串
     * @return 字符串的String类型
     */
    public static String parseEmpty(@Nullable String str) {
        if (str == null || "null".equals(str.trim().toLowerCase())) {
            str = "";
        }
        return str.trim();
    }

    /**
     * Parse the given string to not null string, if the given is null, will
     * return "".
     *
     * @return
     */
    public static String stringNotNull(String str) {
        if (isEmpty(str)) {
            str = "";
        }
        return str;
    }

    /**
     * 描述：判断一个字符串是否为null或空值.
     *
     * @param str 指定的字符串
     * @return true or false
     */
    public static boolean isEmpty(CharSequence str) {
        return TextUtils.isEmpty(str);
    }

    /**
     * 描述：判断一个字符串是否为http请求
     *
     * @param str 指定的字符串
     * @return true or false
     */
    public static boolean isHttpUrl(@NonNull String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.startsWith("http://") || str.startsWith("https://");
    }

    /**
     * 根据Unicode编码完美的判断中文汉字和符号
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    /**
     * 完整的判断中文汉字和符号
     *
     * @param strName
     * @return
     */
    public static boolean isChinese(@NonNull String strName) {
        if (strName.startsWith("·") || strName.endsWith("·")) {
            return false;
        }
        String temp = strName.replace("·", "");
        char[] ch = temp.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!isChinese(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取字符串长度，中文字符2个字节，字母和数字1个字节
     *
     * @param dest
     * @return
     */
    public static int getStrLength(CharSequence dest) {
        int count = 0;
        int dindex = 0;
        while (dindex < dest.length()) {
            char c = dest.charAt(dindex++);
            if (c < 128) {
                count = count + 1;
            } else {
                count = count + 2;
            }
        }
        return count;
    }

    /**
     * 验证护照   eg:E37277459
     *
     * @param passport
     * @return
     */
    public static boolean isPassport(@NonNull String passport) {
        String reg = "^([a-z~A-Z])(\\d{8})$";
        return passport.matches(reg);
    }

    /**
     * 描述：手机号格式验证(仅限大陆).
     *
     * @param phoneNumber 指定的手机号码字符串
     * @return 是否为手机号码格式:是为true，否则false
     */
    public static Boolean isInnerMobileNo(@NonNull String phoneNumber) {
        if (isEmpty(phoneNumber)) {
            return false;
        }
        boolean isValid = false;
        Pattern pattern = Pattern.compile("^[1][3-9]+\\d{9}");
        Matcher matcher = pattern.matcher(phoneNumber);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * 获取隐藏的手机号码（只显示前3位和后4位）。13812345678 -> 138****5678
     *
     * @param phone 手机号码。
     */
    public static String getHidePhone(String phone) {
        if (phone == null || phone.length() < 8) {
            return phone;
        }

        StringBuilder sub = new StringBuilder(phone.substring(0, 3));
        for (int i = 0; i < phone.length() - 7; i++) {
            sub.append("*");
        }
        sub.append(phone.substring(phone.length() - 4, phone.length()));
        return sub.toString();
    }

    /**
     * 描述：是否只是数字.
     *
     * @param str 指定的字符串
     * @return 是否只是数字:是为true，否则false
     */
    @NonNull
    public static Boolean isNumber(@NonNull String str) {
        Boolean isNumber = false;
        String expr = "^[0-9]+$";
        if (str.matches(expr)) {
            isNumber = true;
        }
        return isNumber;
    }

    /**
     * 描述：是否是邮箱.
     *
     * @param str 指定的字符串
     * @return 是否是邮箱:是为true，否则false
     */
    @NonNull
    public static Boolean isEmail(@NonNull String str) {
        Boolean isEmail = false;
        String expr = "^([a-z0-9A-Z_]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        if (str.matches(expr)) {
            isEmail = true;
        }
        return isEmail;
    }

    /**
     * 判断字符串是否为IP地址
     **/
    public static boolean isIPAddress(@NonNull String ipaddr) {
        boolean flag = false;
        Pattern pattern = Pattern
                .compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
        Matcher m = pattern.matcher(ipaddr);
        flag = m.matches();
        return flag;
    }

    /**
     * 身份证第18位（校验码）的计算方法
     * 　　* 1、将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7－9－10－5－8－4－2－1－6－3－7－9－10－5－8－4－2。
     * 　　* 2、将这17位数字和系数相乘的结果相加。
     * 　　* 3、用加出来和除以11，看余数是多少？
     * 　　* 4、余数只可能有0－1－2－3－4－5－6－7－8－9－10这11个数字。其分别对应的最后一位身份证的号码为1－0－X－9－8－7－6－5－4－3－2。
     * 　　* 5、通过上面得知如果余数是2，就会在身份证的第18位数字上出现罗马数字的Ⅹ。如果余数是10，身份证的最后一位号码就是2。
     * 　　* 例如：某男性的身份证号码是34052419800101001X。我们要看看这个身份证是不是合法的身份证。
     * 　　* 首先我们得出前17位的乘积和是189，然后用189除以11得出的结果是17+2/11，也就是说其余数是2。最后通过对应规则就可以知道余数2对应的数字是x。所以，可以判定这是一个合格的身份证号码。
     */
    public static boolean isResidentIdentityNum(@NonNull String num) {
        if (isEmpty(num) || num.length() != 18) {
            return false;
        }
        char[] cadArray = num.toUpperCase().toCharArray();
        int[] ratioArray = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] retArray = new char[]{'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < cadArray.length - 1; i++) {
            int a = 0;
            try {
                a = Integer.parseInt(String.valueOf(cadArray[i]));
            } catch (Exception e) {
                e.printStackTrace();
            }
            sum += a * ratioArray[i];
        }
        return retArray[sum % 11] == cadArray[17];
    }

    /**
     * 描述：从输入流中获得String.
     *
     * @param is 输入流
     * @return 获得的String
     */
    public static String isToStr(@NonNull InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            // 最后一个\n删除
            if (sb.indexOf("\n") != -1 && sb.lastIndexOf("\n") == sb.length() - 1) {
                sb.delete(sb.lastIndexOf("\n"), sb.lastIndexOf("\n") + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * 将byte数组转换为16进制
     *
     * @param b
     * @param separator 分隔符
     * @return
     */
    public static String bytes2HexString(@Nullable byte[] b, String separator) {
        if (b != null) {
            StringBuffer sb = new StringBuffer(b.length * 2);
            for (int i = 0; i < b.length; i++) {
                // look up high nibble char
                // fill left with zero
                sb.append(hexChars[(b[i] & 0xf0) >>> 4]);
                // bits
                // look up low nibble char
                sb.append(hexChars[b[i] & 0x0f]);

                if (!isEmpty(separator) && (i < b.length - 1)) {
                    sb.append(separator);
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * 描述：不足2个字符的在前面补“0”.
     *
     * @param str 指定的字符串
     * @return 至少2个字符的字符串
     */
    public static String strFormatTwoPlace(@NonNull String str) {
        try {
            if (str.length() <= 1) {
                str = "0" + str;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 获取大小的描述.
     *
     * @param size 大小（单位为KB）
     * @return 大小的描述（单位为MB）
     */
    @NonNull
    public static String getSizeDescM(long size) {
        //将KB转换为MB并四舍五入保留小数点后一位
        return Math.round((size / 1024f) * 10) / 10f + "M";
    }

    /**
     * 获取大小的描述.
     *
     * @param size 字节个数
     * @return 大小的描述
     */
    public static String getSizeDescFormat(long size) {
        float tempSize = 0;
        String suffix = "B";
        if (size >= 1024) {
            suffix = "K";
            tempSize = size / 1024f;
            if (tempSize >= 1024) {
                suffix = "M";
                tempSize = tempSize / 1024f;
                if (tempSize >= 1024) {
                    suffix = "G";
                    tempSize = tempSize / 1024f;
                }
            }
        } else {
            tempSize = size;
        }
        return String.format("%.2f%s", tempSize, suffix);
    }

    /**
     * Formats a content size to be in the form of bytes, kilobytes, megabytes, etc.
     * <p>
     * If the context has a right-to-left locale, the returned string is wrapped in bidi formatting
     * characters to make sure it's displayed correctly if inserted inside a right-to-left string.
     * (This is useful in cases where the unit strings, like "MB", are left-to-right, but the
     * locale is right-to-left.)
     *
     * @param context   Context to use to load the localized units
     * @param sizeBytes size value to be formatted, in bytes
     * @return formatted string with the number
     */
    public static String formatFileSize(Context context, long sizeBytes) {
        return Formatter.formatFileSize(context, sizeBytes);
    }

    /**
     * Like {@link #formatFileSize}, but trying to generate shorter numbers
     * (showing fewer digits of precision).
     */
    public static String formatShortFileSize(Context context, long sizeBytes) {
        return Formatter.formatShortFileSize(context, sizeBytes);
    }

    /**
     * 拼接字符串<br>
     * [1,2,3] ==>> 1-2-3
     *
     * @param delimiter 分隔符
     * @return
     */
    public static <T> String join(T[] arrys, CharSequence delimiter) {
        if (arrys == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (T o : arrys) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(o);
        }
        return sb.toString();
    }

    /**
     * 拼接字符串<br>
     * [1,2,3] ==>> 1-2-3
     *
     * @param delimiter 分隔符
     * @return
     */
    public static String join(List<?> arrys, CharSequence delimiter) {
        if (arrys == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object token : arrys) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * 翻转单词  abcdefg -> gfedcba
     *
     * @param source
     * @return
     */
    public static String reverseWords(String source) {
        if (isEmpty(source)) {
            return source;
        }
        String[] words = new String[source.length()];
        for (int i = 0; i < source.length(); i++) {
            words[i] = String.valueOf(source.charAt(i));
        }
        Collections.reverse(Arrays.asList(words));
        return join(words, "");
    }

    /**
     * 翻转句子  the sky is blue -> blue is sky the
     *
     * @param source
     * @return
     */
    public static String reverseSentence(String source) {
        if (isEmpty(source)) {
            return source;
        }
        //' +'表示匹配多个空格
        String[] words = source.trim().split(" +");
        Collections.reverse(Arrays.asList(words));
        return join(words, " ");
    }

    /**
     * unicode 转字符串
     */
    public static String unicode2String(String unicode) {
        unicode = (unicode == null ? "" : unicode);
        if (unicode.indexOf("\\u") == -1) {
            //如果不是unicode码则原样返回
            return unicode;
        }

        StringBuffer string = new StringBuffer();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            if (hex[i].length() == 4) {
                // 转换出每一个代码点
                int data = Integer.parseInt(hex[i], 16);
                // 追加成string
                string.append((char) data);
            } else if (hex[i].length() > 4) {
                String temp = hex[i].substring(0, 4);
                // 转换出每一个代码点
                int data = Integer.parseInt(temp, 16);
                // 追加成string
                string.append((char) data);
                string.append(hex[i].substring(4, hex[i].length()));
            } else {
                string.append(hex[i]);
            }
        }
        return string.toString();
    }
}
