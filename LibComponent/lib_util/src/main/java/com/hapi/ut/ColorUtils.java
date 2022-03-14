package com.hapi.ut;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;

/**
 * 颜色工具类
 * <p>
 * Created by xx on 2017/3/23.
 */
public class ColorUtils {

    private static final String TAG = ColorUtils.class.getSimpleName();

    /**
     * 十进制颜色转换为十六进制颜色
     *
     * @param color 十进制
     * @return 十六进制
     */
    public static String toHexEncoding(int color) {
        String r;
        String g;
        String b;
        StringBuilder sb = new StringBuilder();
        r = Integer.toHexString(Color.red(color));
        g = Integer.toHexString(Color.green(color));
        b = Integer.toHexString(Color.blue(color));
        //判断获取到的R,G,B值的长度 如果长度等于1 给R,G,B值的前边添0
        r = r.length() == 1 ? "0" + r : r;
        g = g.length() == 1 ? "0" + g : g;
        b = b.length() == 1 ? "0" + b : b;
        sb.append(r);
        sb.append(g);
        sb.append(b);
        return sb.toString();
    }

    /**
     * 转换颜色最简单的方式
     *
     * @param color 十进制
     * @return 符合Color处理的格式
     */
    public static int toColor(int color) {
        return color | 0xff000000;
    }

    /**
     * 转换为6位十六进制颜色代码，不含“#”
     *
     * @param color the color
     * @return string string
     */
    public static String toColorString(int color) {
        return toColorString(color, false);
    }

    /**
     * 转换为6位十六进制颜色代码，不含“#”
     *
     * @param color        the color
     * @param includeAlpha the include alpha
     * @return string string
     */
    public static String toColorString(int color, boolean includeAlpha) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));
        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }
        if (red.length() == 1) {
            red = "0" + red;
        }
        if (green.length() == 1) {
            green = "0" + green;
        }
        if (blue.length() == 1) {
            blue = "0" + blue;
        }
        String colorString;
        if (includeAlpha) {
            colorString = alpha + red + green + blue;
            Log.d(TAG, String.format("%d to color string is %s", color, colorString));
        } else {
            colorString = red + green + blue;
            Log.d(TAG, String.format("%d to color string is %s%s%s%s, exclude alpha is %s", color, alpha, red, green, blue, colorString));
        }
        return colorString;
    }

    /**
     * 对TextView、Button等设置不同状态时其文字颜色。
     * 参见：http://blog.csdn.net/sodino/article/details/6797821
     * Modified by liyujiang at 2015.08.13
     *
     * @param normalColor  the normal color
     * @param pressedColor the pressed color
     * @param focusedColor the focused color
     * @param unableColor  the unable color
     * @return the color state list
     */
    public static ColorStateList toColorStateList(int normalColor, int pressedColor, int focusedColor, int unableColor) {
        int[] colors = new int[]{pressedColor, focusedColor, normalColor, focusedColor, unableColor, normalColor};
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
        states[2] = new int[]{android.R.attr.state_enabled};
        states[3] = new int[]{android.R.attr.state_focused};
        states[4] = new int[]{android.R.attr.state_window_focused};
        states[5] = new int[]{};
        return new ColorStateList(states, colors);
    }

    /**
     * To color state list color state list.
     *
     * @param normalColor  the normal color
     * @param pressedColor the pressed color
     * @return the color state list
     */
    public static ColorStateList toColorStateList(int normalColor, int pressedColor) {
        return toColorStateList(normalColor, pressedColor, pressedColor, normalColor);
    }
}
