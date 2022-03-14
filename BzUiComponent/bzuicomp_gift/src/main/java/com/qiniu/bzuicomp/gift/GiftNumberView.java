package com.qiniu.bzuicomp.gift;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;




/**
 * Created by SuperMan on 2018/09/26 0026.
 */

public class GiftNumberView extends LinearLayout {
    private static final String TAG = "Scroller";

    private Scroller mScroller;

    // 每个数字的高
    int item_height;
    // 0-9 一轮数字的高度
    int CIRCLE_HEIGHT;
    Context context;

    public final static int DURATION = 300;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == -1) {
                setFinal(0, 0);
            } else if (!mScroller.isFinished() && isShown()) {
                handler.removeCallbacksAndMessages(null);
                handler.sendEmptyMessageDelayed(msg.what, 100);
            } else {
                int number = msg.what;
                if (number == -1) {
                    setFinal(0, 0);
                } else {

                    int currY = mScroller.getCurrY();
                    // 首先判断当前数字是否在第二轮，是的话把数字定位到第一轮的同一数字，因为数字只能往下滚，要留足滚动空间
                    if (currY > CIRCLE_HEIGHT) {
                        setFinal(0, mScroller.getCurrY() - CIRCLE_HEIGHT);
                    }
                    int lastY = mScroller.getFinalY();
                    int y = number * item_height;
                    // 判断新的position是否比原来的小，是的话要跳滚动到下一轮数字中，保证数字永远都是向下滚动的
                    if (lastY > y) {
                        smoothScrollTo(0, CIRCLE_HEIGHT + y, DURATION);
                    } else {
                        smoothScrollTo(0, y, DURATION);
                    }
                }
            }
            return false;
        }
    });

    private int[] numbers = {R.mipmap.num0, R.mipmap.num1, R.mipmap.num2, R.mipmap.num3, R.mipmap.num4
            , R.mipmap.num5, R.mipmap.num6, R.mipmap.num7, R.mipmap.num8, R.mipmap.num9, R.mipmap.num0, R.mipmap.num1, R.mipmap.num2, R.mipmap.num3, R.mipmap.num4
            , R.mipmap.num5, R.mipmap.num6, R.mipmap.num7, R.mipmap.num8, R.mipmap.num9};

    public GiftNumberView(Context context) {
        super(context);
        init(context);
    }

    public GiftNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
        item_height = dip2px(context, 36);
        CIRCLE_HEIGHT = item_height * 10;
        setOrientation(VERTICAL);
        for (int i = 0; i < numbers.length; i++) {
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(dip2px(context, 25), dip2px(context, 36));
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(numbers[i]);
            imageView.setLayoutParams(layoutParams);
            addView(imageView);
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(dip2px(context, 25), dip2px(context, 36));
        setLayoutParams(layoutParams);
        // 初始化位置到0，运营反馈进位时会有一瞬间是数字9
        setFinal(0, 0);
    }

    public void forceFinished() {
        // 相当于暂停了正在进行的滑动动画
        mScroller.forceFinished(true);
    }

    public void reset() {
//        handler.sendEmptyMessage(-1);
    }

    public void destroy() {

        handler.removeCallbacksAndMessages(null);
//        handler = null;
    }

    //设置mScroller最终停留的\位置，没有动画效果，直接跳到目标位置
    private void setFinal(int newX, int newY) {
//        LogUtil.e("computeScroll setFinal");

        if (mScroller.getCurrY() > 0) {
            // 数字只会往上滚，体验好，但在部分机型上会失效，一直为0
            mScroller.setFinalX(newX);
            mScroller.setFinalY(newY);
            invalidate();
        } else {
            // 会向上滚或向下滚，效果不统一，体验一般,但没有兼容问题
            scrollTo(newX, newY);
            postInvalidate();
        }
    }

    // 平滑滚动到某个数字
    public void smoothToPosition(int number) {
        handler.sendEmptyMessage(number);
    }

    // 立刻跳到某个位置
    public void scrollToPosition(int number) {
        setVisibility(VISIBLE);
        setFinal(0, number * item_height);
    }

    //调用此方法滚动到目标位置
    private void smoothScrollTo(int fx, int fy, int duration) {
        setVisibility(VISIBLE);
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy, duration);
    }

    //调用此方法设置滚动的相对偏移
    private void smoothScrollBy(int dx, int dy, int duration) {
//        LogUtil.e("computeScroll smoothScrollBy");
        //设置mScroller的滚动偏移量
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy, duration);
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

    @Override
    public void computeScroll() {
//        LogUtil.e("computeScroll 1");
        //先判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset()) {
//            LogUtil.e("computeScroll 2 " + mScroller.getCurrX() + "---" + mScroller.getCurrY());
            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        try {
            if (context != null) {
                if (context.getResources() != null) {
                    final float scale = context.getResources().getDisplayMetrics().density;
                    return (int) (dipValue * scale + 0.5f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


}
