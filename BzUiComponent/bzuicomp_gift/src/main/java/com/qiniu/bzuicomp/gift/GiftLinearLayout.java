package com.qiniu.bzuicomp.gift;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;



/**
 * Created by SuperMan on 2018/10/08 0008.
 * 0507 去掉了礼物数字的滚动队列，改为新动画覆盖老动画
 */

public class GiftLinearLayout extends LinearLayout implements Animator.AnimatorListener {
    private ObjectAnimator objectAnimator;

    public GiftLinearLayout(Context context) {
        super(context);
        init(context);
    }

    public GiftLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        PropertyValuesHolder animatorX = PropertyValuesHolder.ofFloat("scaleX", 1.4f, 0.7f, 0.9f, 1.0f);
        PropertyValuesHolder animatorY = PropertyValuesHolder.ofFloat("scaleY", 1.4f, 0.7f, 0.9f, 1.0f);
        objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, animatorX, animatorY);
        objectAnimator.setDuration(100);

        // 预先往里面塞几个数字
        for (int i = 0; i <= 8; i++) {
            GiftNumberView numberView = new GiftNumberView(context);
            addView(numberView);
            numberView.setVisibility(GONE);
        }
    }

    private volatile int lastNumber;

    // 入口类
    public void addNumber(int count) {
        int type = GiftNumbBean.SCALE_NUMB;
        if (count - lastNumber >= 9) {
            type = GiftNumbBean.SCROOL_NUMB;
        }
        GiftNumbBean giftNumbBean = new GiftNumbBean(count, type);
        startAnimation(giftNumbBean);
        lastNumber = count;
    }

    public void reset() {
//        for (int i = 0; i < getChildCount(); i++) {
//            GiftNumberView giftNumberView = (GiftNumberView) getChildAt(i);
//            giftNumberView.reset();
//        }
        lastNumber = 0;
    }

    public void destroy() {
        for (int i = 0; i < getChildCount(); i++) {
            GiftNumberView giftNumberView = (GiftNumberView) getChildAt(i);
            giftNumberView.reset();
            giftNumberView.destroy();
        }
        lastNumber = 0;
    }

    private void loadNumber(int count) {
        char[] arrStr = String.valueOf(count).toCharArray();
        int numberLength = arrStr.length;

        // 显示要展示的前几位数字
        for (int i = 0; i < numberLength; i++) {
            if (i < getChildCount()) {
                GiftNumberView giftNumberView = (GiftNumberView) getChildAt(i);
                giftNumberView.setVisibility(View.VISIBLE);
            }
        }

        // 对不需要显示的礼物数字位数要隐藏
        for (int i = numberLength; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(View.GONE);
        }

    }

    private void startAnimation(GiftNumbBean giftNumbBean) {
        loadNumber(giftNumbBean.getCount());
        if (giftNumbBean.getType() == GiftNumbBean.SCROOL_NUMB) {
            startScrollAni(giftNumbBean);
        } else {
            startScaleAni(giftNumbBean);
        }
    }

    private void startScrollAni(GiftNumbBean giftNumbBean) {
        char[] arrStr = String.valueOf(giftNumbBean.getCount()).toCharArray();

        for (int i = 0; i < arrStr.length; i++) {
            String anArrStr = String.valueOf(arrStr[i]);
            if (!TextUtils.isEmpty(anArrStr)) {
                GiftNumberView numberView = (GiftNumberView) getChildAt(i);
                numberView.smoothToPosition(Integer.valueOf(anArrStr));
            }
        }
    }

    private void startScaleAni(GiftNumbBean giftNumbBean) {

        char[] arrStr = String.valueOf(giftNumbBean.getCount()).toCharArray();
        for (int i = 0; i < arrStr.length; i++) {
            String anArrStr = String.valueOf(arrStr[i]);
            if (!TextUtils.isEmpty(anArrStr)) {
                GiftNumberView numberView = (GiftNumberView) getChildAt(i);
                numberView.scrollToPosition(Integer.valueOf(anArrStr));
            }
        }
        if (objectAnimator != null) {
            objectAnimator.start();
        }
    }

    // 缩放数字动画监听器
    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {

    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
