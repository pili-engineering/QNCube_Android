package com.hapi.ut.draw;

import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;

import com.hapi.ut.ColorUtils;


/**
 * Created by xx on 16/6/2.
 */
public class DrawableHelper {

    private static final int TRANS_COLOR = android.R.color.transparent;

    /**
     * 动态设置drawable资源文件
     *
     * @return
     */
    public static GradientDrawable getGradientDrawable(float radius, String colorInt, String bgColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(radius);
        drawable.setStroke(1, stringToColor(colorInt));
        drawable.setColor(stringToColor(bgColor));

        return drawable;
    }

    public static int stringToColor(String str) {
        if (TextUtils.isEmpty(str) || str.length() < 6) {
            return TRANS_COLOR;
        }
        return Color.parseColor("#" + str);
    }

    /**
     * 获取动态Drawable
     *
     * @param colorInt
     * @param radius
     */
    public static GradientDrawable getGradientDrawable(int colorInt, int radius) {
        int color = Color.parseColor(
                "#" + ColorUtils.toHexEncoding(colorInt));
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{color, 0x00000000});
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    public static GradientDrawable getGradientDrawable(int[] colorInt, int radius, int shape
            , GradientDrawable.Orientation orientation) {
        GradientDrawable drawable = new GradientDrawable(
                orientation,
                colorInt);
        drawable.setShape(shape);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    /**
     * 获取普通Drawable文件 无渐变色
     *
     * @param colorInt
     * @param radius
     * @return
     */
    public static GradientDrawable getNormalGradientDrawable(@ColorInt int colorInt, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(radius);
        drawable.setColor(colorInt);
        return drawable;
    }

    /**
     * 针对progressBar动态设置不同的背景以及进度背景
     *
     * @param progressDrawable
     * @param background
     * @return
     */
    public static LayerDrawable getProgressBarDrawableByDrawable(@NonNull Drawable progressDrawable, @NonNull Drawable background) {
        ClipDrawable clipDrawable = new ClipDrawable(progressDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
        Drawable[] layers = new Drawable[]{background, clipDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);
        return layerDrawable;
    }

    /**
     * 针对progressBar动态设置不同的背景以及进度背景
     *
     * @param progressColorInt
     * @param backgroundColorInt
     * @return
     */
    public static LayerDrawable getProgressBarDrawableByColor(@ColorInt int progressColorInt, @ColorInt int backgroundColorInt, int progressRadius) {
        GradientDrawable progressDrawable = getNormalGradientDrawable(progressColorInt, progressRadius);
        GradientDrawable background = getNormalGradientDrawable(backgroundColorInt, progressRadius);
        if (progressDrawable == null || background == null) {
            return null;
        }
        ClipDrawable clipDrawable = new ClipDrawable(progressDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
        Drawable[] layers = new Drawable[]{background, clipDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);
        return layerDrawable;
    }
}
