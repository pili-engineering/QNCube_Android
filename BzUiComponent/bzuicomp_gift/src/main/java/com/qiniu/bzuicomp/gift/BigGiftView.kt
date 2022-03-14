package com.qiniu.bzuicomp.gift

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.gifdecoder.GifDecoder
import com.bumptech.glide.gifdecoder.GifHeaderParser
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class BigGiftView : FrameLayout, IBigGiftView<GiftMsg> {

    lateinit var imageView: AppCompatImageView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val layout = FrameLayout(getContext())
        imageView = AppCompatImageView(getContext())
        layout.addView(imageView)
        addView(layout)
      //  imageView.setImageResource(R.drawable.gift_01_bell)
    }

    override var finishedCall: (() -> Unit)?=null
    override var isPlaying = false
    override fun getView(): View {
        return this
    }
    override fun clear() {
    }

    override fun playIfPlayAble(gigGiftMode: GiftMsg): Boolean {
        isPlaying = true
        Glide.with(context)
            .asGif()
            .load(DataInterfaceNew.getGiftAnimRes(gigGiftMode.sendGift.giftId.toInt()))
            .listener(object : RequestListener<GifDrawable?> {
                private fun getSelfStoppedGifDrawable(drawable: GifDrawable): GiftGifDrawable {
                    val provider = GifBitmapProvider(Glide.get(context).bitmapPool)
                    var transformation:  Transformation<Bitmap>? = drawable.frameTransformation
                    if (transformation == null) {
                        transformation = CenterCrop()
                    }
                    val byteBuffer = drawable.buffer
                    val decoder = StandardGifDecoder(provider)
                    decoder.setData(GifHeaderParser().setData(byteBuffer).parseHeader(), byteBuffer)
                    var bitmap = drawable.firstFrame
                    if (bitmap == null) {
                        decoder.advance()
                        bitmap = decoder.nextFrame
                    }
                    return GiftGifDrawable(context, decoder, transformation, 0, 0, bitmap)
                }


                override fun onLoadFailed(
                    e: GlideException?, model: Any,
                    target: Target<GifDrawable?>, isFirstResource: Boolean
                ): Boolean {
                    isPlaying = false
                    finishedCall?.invoke()
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?, model: Any,
                    target: Target<GifDrawable?>, dataSource: DataSource, isFirstResource: Boolean
                ): Boolean {
                    val giftDrawable: GiftGifDrawable = getSelfStoppedGifDrawable(resource!!)?:return false
                    var delay = 0
                    for (i in 0 until giftDrawable.gifDecoder.frameCount) {
                        delay += giftDrawable.gifDecoder.getDelay(i)
                    }
                    Handler(context.mainLooper).postDelayed(
                        {
                            imageView.setImageBitmap(null)
                            isPlaying = false
                            finishedCall?.invoke()
                        },
                        delay.toLong()
                    )
                    return false
                }
            })
            .into(imageView)

        return true
    }
    class GiftGifDrawable internal constructor(
        context: Context?,
        var gifDecoder: GifDecoder,
        frameTransformation: Transformation<Bitmap>,
        targetFrameWidth: Int,
        targetFrameHeight: Int,
        firstFrame: Bitmap?
    ) :
        GifDrawable(
            context,
            gifDecoder, frameTransformation, targetFrameWidth, targetFrameHeight, firstFrame
        )

}