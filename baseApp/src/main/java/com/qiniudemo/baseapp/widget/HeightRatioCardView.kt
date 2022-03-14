package com.qiniudemo.baseapp.widget

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.qiniu.baseapp.R

class HeightRatioCardView : CardView {

    //高/宽
    var heightRatio = 0.0
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(
        context, attrs, -1
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.heightRatio)
        typedArray?.apply {
            val ratio = getFloat(R.styleable.heightRatio_ratio, 0.0f)
            if (ratio != 0.0f) {
                heightRatio = ratio.toDouble()
            }
        }
        typedArray?.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        if (heightRatio == 0.0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSize == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val newMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        val newMeasureSpecHeight =
            MeasureSpec.makeMeasureSpec((widthSize * heightRatio).toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(newMeasureSpec, newMeasureSpecHeight)
    }
}