package com.qiniu.bzuicomp.gift;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.gifdecoder.GifHeaderParser;
import com.bumptech.glide.gifdecoder.StandardGifDecoder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.gif.GifBitmapProvider;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.nio.ByteBuffer;

public class GiftAnimWindow extends Dialog {
    private static final String TAG = GiftAnimWindow.class.getSimpleName();
    private int mResource;

    public GiftAnimWindow(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setAnimResource(int resource) {
        mResource = resource;
    }

    @Override
    public void show() {
        FrameLayout layout = new FrameLayout(getContext());
        AppCompatImageView imageView = new AppCompatImageView(getContext());
        layout.addView(imageView);
        setContentView(layout);
        Glide.with(getContext()).asGif().load(mResource).listener(new RequestListener<GifDrawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                        Target<GifDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, Object model,
                                           Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                GiftGifDrawable giftDrawable = getSelfStoppedGifDrawable(resource);
                int delay = 0;
                for (int i = 0; i < giftDrawable.gifDecoder.getFrameCount(); i++) {
                    delay += giftDrawable.gifDecoder.getDelay(i);
                }

                new Handler(getContext().getMainLooper()).postDelayed(GiftAnimWindow.this::dismiss, delay);
                return false;
            }
        }).into(imageView);
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private GiftGifDrawable getSelfStoppedGifDrawable(GifDrawable drawable) {
        GifBitmapProvider provider = new GifBitmapProvider(Glide.get(getContext()).getBitmapPool());
        Transformation transformation = drawable.getFrameTransformation();
        if (transformation == null) {
            transformation = new CenterCrop();
        }

        ByteBuffer byteBuffer = drawable.getBuffer();
        StandardGifDecoder decoder = new StandardGifDecoder(provider);
        decoder.setData(new GifHeaderParser().setData(byteBuffer).parseHeader(),byteBuffer);
        Bitmap bitmap = drawable.getFirstFrame();
        if (bitmap == null) {
            decoder.advance();
            bitmap = decoder.getNextFrame();
        }

        return new GiftGifDrawable(getContext(), decoder, transformation, 0, 0, bitmap);
    }

    private static class GiftGifDrawable extends GifDrawable {
        GifDecoder gifDecoder;
        GiftGifDrawable(Context context, GifDecoder gifDecoder, Transformation<Bitmap> frameTransformation,
                               int targetFrameWidth, int targetFrameHeight, Bitmap firstFrame) {
            super(context, gifDecoder, frameTransformation, targetFrameWidth, targetFrameHeight, firstFrame);
            this.gifDecoder = gifDecoder;
        }
    }
}
