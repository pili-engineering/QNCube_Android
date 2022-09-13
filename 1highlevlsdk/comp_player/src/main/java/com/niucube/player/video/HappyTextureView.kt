package com.niucube.player.video

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import com.niucube.player.PlayerStatus
import com.niucube.player.utils.LogUtil
import com.niucube.player.utils.setScale

/**
 *
 */
internal class HappyTextureView : TextureView {

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    private var videoHeight: Int = 0
    private var videoWidth: Int = 0
    var tagNam =""
    private var centerCropError = 0f

    private var mViewRotation = 0f
    fun adaptVideoSize(vWidth: Int, vHeight: Int) {

        var widthMeasureSpec = vWidth
        var heightMeasureSpec = vHeight
        if (this.videoWidth != widthMeasureSpec || this.videoHeight != heightMeasureSpec) {
            this.videoWidth = widthMeasureSpec
            this.videoHeight = heightMeasureSpec
            requestLayout()
        }
    }

    override fun setRotation(r: Float) {
        if (rotation != r) {
            super.setRotation(r)
            mViewRotation =r
            requestLayout()
        }
    }

    var parentWindType: (() -> Int)? = null

    fun setCenterCropError(centerCropError: Float) {
        this.centerCropError = centerCropError
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec

        if (mViewRotation == 90f || mViewRotation == 270f) {
            val tempMeasureSpec = widthMeasureSpec
            widthMeasureSpec = heightMeasureSpec
            heightMeasureSpec = tempMeasureSpec
        }

        var widthV = videoWidth
        var heightV = videoHeight

        val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)
        //控件宽高
//        Log.d("HappyTextureView", )
        LogUtil.d(tagNam+"   texture onMeasure" + heightSpecSize+"    w"+widthSpecSize)
        if (videoWidth > 0 && videoHeight > 0) {

            val hightRatio = heightSpecSize / heightV.toDouble()
            val withRatio = widthSpecSize / widthV.toDouble()
            /**
             * 横瓶播放器　播放横屏视频　
             */
            if (((widthV > heightV && widthSpecSize > heightSpecSize)

                        || (widthV < heightV && widthSpecSize < heightSpecSize))

                && parentWindType?.invoke() != PlayerStatus.MODE_FULL_SCREEN

            ) {
                val max = if (hightRatio < withRatio) {
                    withRatio
                } else {
                    hightRatio
                }

                val tempW = (heightV * max - heightSpecSize) / heightSpecSize
                val tempH = (widthV * max - widthSpecSize) / widthSpecSize

                LogUtil.d(tagNam+"centenrCrop ——>      tempH   "+Math.abs(tempH).setScale(2)
                        +"  tem"+ Math.abs(tempW).setScale(2)
                        +"  centerCropError "+centerCropError+"  videoWidth "+videoWidth+"   videoHeight"+videoHeight
                        +" heightSpecSize"+heightSpecSize+"   widthSpecSize  "+widthSpecSize

                )
                if (Math.abs(tempH) < centerCropError && Math.abs(tempW) < centerCropError) {
                    heightV = (heightV * max).toInt()
                    widthV = (widthV * max).toInt()

                    super.onMeasure(MeasureSpec.makeMeasureSpec(widthV,MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightV,MeasureSpec.EXACTLY))
                    return

                }
            }

            if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                // the size is fixed
                widthV = widthSpecSize
                heightV = heightSpecSize
                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * heightV < widthV * videoHeight) {
                    widthV = heightV * videoWidth / videoHeight
                } else if (videoWidth * heightV > widthV * videoHeight) {
                    heightV = widthV * videoHeight / videoWidth
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                widthV = widthSpecSize
                heightV = widthV * videoHeight / videoWidth
                if (heightSpecMode == View.MeasureSpec.AT_MOST && heightV > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    heightV = heightSpecSize
                    widthV = heightV * videoWidth / videoHeight
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                heightV = heightSpecSize
                widthV = heightV * videoWidth / videoHeight
                if (widthSpecMode == View.MeasureSpec.AT_MOST && widthV > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    widthV = widthSpecSize
                    heightV = widthV * videoHeight / videoWidth
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                widthV = videoWidth
                heightV = videoHeight
                if (heightSpecMode == View.MeasureSpec.AT_MOST && heightV > heightSpecSize) {
                    // too tall, decrease both width and height
                    heightV = heightSpecSize
                    widthV = heightV * videoWidth / videoHeight
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && widthV > widthSpecSize) {
                    // too wide, decrease both width and height
                    widthV = widthSpecSize
                    heightV = widthV * videoHeight / videoWidth
                }
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(widthV,MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightV,MeasureSpec.EXACTLY))
            return
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}

