package com.qiniudemo.baseapp.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;

public class TipProgressBar extends View {

    private int mWidth;
    private int mHeight;
    private int mViewHeight;

    private Paint mProgressBackgroundPaint;
    private Paint mProgressForegroundPaint;
    private RectF mProgressBackgroundRectF = new RectF();
    private RectF mProgressForegroundRectF = new RectF();
    private int mProgressRoundRectRadius;
    private int mProgressPaintWidth;
    private float mCurrentProgress;
    private int mProgressHeight;
    private float mMoveDis;
    private int mBgColor = 0xFFe1e5e8;
    private int mProgressColor = 0xFF1E8BFF;

    private int mProgressTipWidth;
    private int mProgressTipHeight;
    private Paint mProgressTipPaint;
    private int mProgressTipPaintWidth;
    private RectF mProgressTipRectF = new RectF();
    private int mProgressTipRoundRectRadius;
    private Path mTrianglePath = new Path();
    private int mTriangleHeight;
    private int mProgressMarginTop;

    private Paint mProgressTipTextPaint;
    private Rect mProgressTipTextRect = new Rect();
    private String mProgressTipTextString = "0";
    private int mProgressTipTextPaintSize;

    private Paint mTextTipTextPaint;
    private int mTextTipMarginTop;
    private String mTextTipTextString = "";
    private int mTextTipTextPaintSize;
    private int mTextTipHeight;

    private ProgressListener mProgressListener;

    public TipProgressBar(Context context) {
        super(context);
    }

    public TipProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        initPaint();
    }

    /**
     * 初始化画笔宽度及 view 大小
     */
    private void init() {
        mProgressHeight = dp2px(7);
        mProgressMarginTop = dp2px(8);
        mProgressRoundRectRadius = dp2px(14);
        mProgressPaintWidth = dp2px(1);
        mTriangleHeight = dp2px(3);
        mProgressTipRoundRectRadius = dp2px(2);
        mProgressTipHeight = dp2px(15);
        mProgressTipWidth = dp2px(40);
        mProgressTipPaintWidth = dp2px(1);
        mProgressTipTextPaintSize = sp2px(10);
        mTextTipMarginTop = dp2px(8);
        mTextTipHeight = dp2px(6);
        mTextTipTextPaintSize = sp2px(10);

        // view 真实的高度
        mViewHeight = mProgressTipHeight + mTriangleHeight + mProgressHeight + mProgressMarginTop
                + mTextTipMarginTop + mTextTipHeight;
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mProgressBackgroundPaint = getShapePaint(mProgressPaintWidth, mBgColor, Paint.Style.FILL);
        mProgressForegroundPaint = getShapePaint(mProgressPaintWidth, mProgressColor, Paint.Style.FILL);
        mProgressTipPaint = getShapePaint(mProgressTipPaintWidth, mProgressColor, Paint.Style.FILL);

        mProgressTipTextPaint = getTextPaint(mProgressTipTextPaintSize, Color.WHITE, Paint.Align.CENTER);
        mTextTipTextPaint = getTextPaint(mTextTipTextPaintSize, Color.GRAY, Paint.Align.LEFT);
    }

    /**
     * 初始化文字画笔
     *
     * @param textSize
     * @param textColor
     * @param textAlign
     * @return
     */
    private Paint getTextPaint(int textSize, int textColor, Paint.Align textAlign) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(textAlign);
        paint.setAntiAlias(true);
        return paint;
    }

    /**
     * 初始化图形画笔
     *
     * @param strokeWidth
     * @param color
     * @param style
     * @return
     */
    private Paint getShapePaint(int strokeWidth, int color, Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(style);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(measureWidth(widthMode, width), measureHeight(heightMode, height));
    }

    /**
     * 测量宽度
     *
     * @param mode
     * @param width
     * @return
     */
    private int measureWidth(int mode, int width) {
        switch (mode) {
            case MeasureSpec.EXACTLY:
                mWidth = width;
                break;
            default:
                break;
        }
        return mWidth;
    }

    /**
     * 测量高度
     *
     * @param mode
     * @param height
     * @return
     */
    private int measureHeight(int mode, int height) {
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                mHeight = mViewHeight;
                break;
            case MeasureSpec.EXACTLY:
                mHeight = height;
                break;
            default:
                break;
        }
        return mHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制进度条
        drawProgressView(canvas);
        // 绘制进度提示的显示框
        drawTipView(canvas);
        // 绘制文字
        drawText(canvas);
    }

    /**
     * 绘制进度条 view
     * @param canvas
     */
    private void drawProgressView(Canvas canvas) {
        // 绘制进度条背景
        mProgressBackgroundRectF.set(getPaddingLeft(), mProgressTipHeight + mProgressMarginTop,
                getWidth(), mProgressTipHeight + mProgressMarginTop + mProgressHeight);
        canvas.drawRoundRect(mProgressBackgroundRectF, mProgressRoundRectRadius,
                mProgressRoundRectRadius, mProgressBackgroundPaint);
        // 绘制进度条当前进度
        mProgressForegroundRectF.set(getPaddingLeft(), mProgressTipHeight + mProgressMarginTop,
                mCurrentProgress, mProgressTipHeight + mProgressMarginTop + mProgressHeight);
        canvas.drawRoundRect(mProgressForegroundRectF, mProgressRoundRectRadius,
                mProgressRoundRectRadius, mProgressForegroundPaint);
    }

    /**
     * 绘制进度上边提示百分比的 view
     *
     * @param canvas
     */
    private void drawTipView(Canvas canvas) {
        drawRoundRect(canvas);
        drawTriangle(canvas);
    }

    /**
     * 绘制圆角矩形
     *
     * @param canvas
     */
    private void drawRoundRect(Canvas canvas) {
        mProgressTipRectF.set(mMoveDis, 0, mProgressTipWidth + mMoveDis, mProgressTipHeight);
        canvas.drawRoundRect(mProgressTipRectF, mProgressTipRoundRectRadius, mProgressTipRoundRectRadius, mProgressTipPaint);
    }

    /**
     * 绘制三角形
     *
     * @param canvas
     */
    private void drawTriangle(Canvas canvas) {
        mTrianglePath.moveTo(mProgressTipWidth / 2 - mTriangleHeight + mMoveDis, mProgressTipHeight);
        mTrianglePath.lineTo(mProgressTipWidth / 2 + mMoveDis, mProgressTipHeight + mTriangleHeight);
        mTrianglePath.lineTo(mProgressTipWidth / 2 + mTriangleHeight + mMoveDis, mProgressTipHeight);
        canvas.drawPath(mTrianglePath, mProgressTipPaint);
        mTrianglePath.reset();
    }

    /**
     * 绘制文字
     *
     * @param canvas 画布
     */
    private void drawText(Canvas canvas) {
        mProgressTipTextRect.left = (int) mMoveDis;
        mProgressTipTextRect.top = 0;
        mProgressTipTextRect.right = (int) (mProgressTipWidth + mMoveDis);
        mProgressTipTextRect.bottom = mProgressTipHeight;
        Paint.FontMetricsInt progressTipTextFontMetrics = mProgressTipTextPaint.getFontMetricsInt();
        int baseline = (mProgressTipTextRect.bottom + mProgressTipTextRect.top
                - progressTipTextFontMetrics.bottom - progressTipTextFontMetrics.top) / 2;
        canvas.drawText(mProgressTipTextString + "%", mProgressTipTextRect.centerX(), baseline, mProgressTipTextPaint);

        int y = mProgressTipHeight + mTriangleHeight + mProgressMarginTop + mProgressHeight + mTextTipMarginTop;
        canvas.drawText(mTextTipTextString, 0, y, mTextTipTextPaint);
    }

    /**
     * 设置当前显示进度
     *
     * @param progress
     * @return
     */
    public void setCurrentProgress(float progress) {
        mCurrentProgress = progress * mWidth / 100;
        mProgressTipTextString = formatNumTwo(progress);

        // 移动百分比提示框，只有当前进度到提示框中间位置之后开始移动，
        // 当进度框移动到最右边的时候停止移动，但是进度条还可以继续移动
        // moveDis 是 tip 框移动的距离
        if (mCurrentProgress >= (mProgressTipWidth / 2) && mCurrentProgress <= (mWidth - mProgressTipWidth / 2)) {
            mMoveDis = mCurrentProgress - mProgressTipWidth / 2;
        }

        if (mProgressListener != null) {
            mProgressListener.currentProgressListener(mCurrentProgress);
        }

        invalidate();
    }

    /**
     * 设置底部文字提示
     *
     * @param text
     */
    public void setTipText(String text) {
        mTextTipTextString = text;
        invalidate();
    }

    /**
     * 回调接口
     */
    public interface ProgressListener {
        void currentProgressListener(float currentProgress);
    }

    /**
     * 回调监听事件
     *
     * @param listener
     * @return
     */
    public TipProgressBar setProgressListener(ProgressListener listener) {
        mProgressListener = listener;
        return this;
    }

    /**
     * 格式化数字(保留两位小数)
     *
     * @param money
     * @return
     */
    public static String formatNumTwo(double money) {
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(money);
    }

    /**
     * dp to px
     *
     * @param dpVal
     */
    private int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp to px
     *
     * @param spVal
     * @return
     */
    private int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, getResources().getDisplayMetrics());
    }
}
