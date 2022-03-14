package com.niucube.audioroom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class UpTriangleView extends View {
    private static final int VIEW_COLOR = Color.rgb(0, 136, 235);
    private static final int STROKE_COLOR = Color.WHITE;

    private Paint mPaint;
    private Path mSolidPath;
    private Path mStrokePath;
    private int mStrokeWidth;
    private float mStrokeSlope;

    public UpTriangleView(Context context) {
        super(context);
        initShape();
    }

    public UpTriangleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initShape();
    }

    private void initShape() {
        mStrokeWidth =2;
        mStrokeSlope = mStrokeWidth * 1.5f;
        mPaint = new Paint();
        mSolidPath = new Path();
        mStrokePath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        int w;
        int h;
        if (measuredWidth >= measuredHeight * 2) {
            h = measuredHeight;
            w = measuredHeight * 2;
        } else {
            h = measuredWidth / 2;
            w = measuredWidth;
        }

        float center = measuredWidth / 2f;
        float half = w / 2;
        mSolidPath.moveTo( center - half, measuredHeight);
        mSolidPath.lineTo(center, measuredHeight - h);
        mSolidPath.lineTo(center + half, measuredHeight);
        mSolidPath.close();
        mPaint.setColor(VIEW_COLOR);
        canvas.drawPath(mSolidPath, mPaint);

        // Draw outline
        mPaint.setColor(STROKE_COLOR);
        mStrokePath.moveTo(center - half - mStrokeSlope, measuredHeight);
        mStrokePath.lineTo(center, measuredHeight - h - mStrokeSlope);
        mStrokePath.lineTo(center + half + mStrokeSlope, measuredHeight);
        mStrokePath.lineTo(center + half, measuredHeight);
        mStrokePath.lineTo(center, measuredHeight - h);
        mStrokePath.lineTo(center - half, measuredHeight);
        mStrokePath.close();

        canvas.drawPath(mStrokePath, mPaint);
    }
}
