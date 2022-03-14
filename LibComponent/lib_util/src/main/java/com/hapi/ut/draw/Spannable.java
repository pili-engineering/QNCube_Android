package com.hapi.ut.draw;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;

import com.hapi.ut.AppCache;
import com.hapi.ut.R;
import com.hapi.ut.callback.Callback;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xx on 16/5/16.
 */
public class Spannable {

    public static CharSequence tintText(Context context, String raw) {
        return tintText(context, raw, R.color.colorAccent);
    }

    public static CharSequence tintText(Context context, String raw, @ColorRes int colorRes) {
        if (TextUtils.isEmpty(raw)) {
            return raw;
        }

        SpannableString spannable = new SpannableString(raw);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(colorRes));
        spannable.setSpan(colorSpan, 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    public static CharSequence tintText(String raw, String target) {
        return tintText(raw, target, R.color.colorAccent);
    }


    public static CharSequence tintText(String raw, String target, @ColorRes int colorRes) {
        if (TextUtils.isEmpty(raw) || TextUtils.isEmpty(target)) {
            return raw;
        }

        SpannableString spannable = new SpannableString(raw);

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(AppCache.getContext().getResources().getColor(colorRes));

        int spannedStart = raw.indexOf(target);
        int spannedEnd = spannedStart + target.length();

        spannable.setSpan(colorSpan, spannedStart, spannedEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    public static CharSequence tintTextColor(String raw, String color) {
        if (TextUtils.isEmpty(raw) || TextUtils.isEmpty(color)) {
            return raw;
        }
        SpannableString spannable = new SpannableString(raw);

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor(color));

        int spannedEnd = raw.length();

        spannable.setSpan(colorSpan, 0, spannedEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    public static CharSequence setTextBold(Context context, String content, int startIndex, int endIndex, @ColorRes int colorRes) {
        if (TextUtils.isEmpty(content) || startIndex < 0 || endIndex >= content.length() || startIndex >= endIndex) {
            return null;
        }

        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(colorRes)), startIndex,
                endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static CharSequence setTextBoldNew(Context context, String content, String target, @ColorRes int colorRes) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        int spannedStart = content.indexOf(target);
        int spannedEnd = spannedStart + target.length();
        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), spannedStart, spannedEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(colorRes)), spannedStart,
                spannedEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }


    public static CharSequence addDrawable(CharSequence source, Drawable drawable1) {
        if (drawable1 == null) {
            return source;
        }

        SpannableString spannable = new SpannableString(source + "^");

        drawable1.setBounds(0, 0, drawable1.getIntrinsicWidth(), drawable1.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(drawable1, ImageSpan.ALIGN_BOTTOM);

        spannable.setSpan(span, spannable.length() - 1, spannable.length(), android.text.Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    public static class Builder {

        private static class Element {
            static final int TYPE_COMPOSE = -1;

            static final int TYPE_DRAWABLE = 0;
            static final int TYPE_STRING = 1;
            static final int TYPE_TINT_STRING = 2;
            static final int TYPE_CLICKABLE_STRING = 3;
            static final int TYPE_DRAWABLE_VERTICAL = 4;
            static final int TYPE_RELATIVE_SIZE = 5;

            int type;
            int length;
            String s;
            float ratio;
            Drawable drawable;
            int align = ImageSpan.ALIGN_BASELINE;
            int color;
            View.OnClickListener onClickListener;
            Callback<SpannableStringBuilder> composeCall;

            private Element(Drawable drawable, int align, View.OnClickListener... listener) {
                if (align != ImageSpan.ALIGN_BASELINE
                        && align != ImageSpan.ALIGN_BOTTOM) {
                    throw new IllegalArgumentException("The align can only be " +
                            "ImageSpan.ALIGN_BASELINE or ImageSpan.ALIGN_BOTTOM");
                }
                type = TYPE_DRAWABLE;
                this.drawable = drawable;
                this.align = align;
                length = 1;
                s = " ";
                if (listener.length != 0) {
                    onClickListener = listener[0];
                }

            }

            private Element(Callback<SpannableStringBuilder> composeCall) {
                type = TYPE_COMPOSE;
                this.composeCall = composeCall;
            }

            private Element(Drawable drawable) {
                type = TYPE_DRAWABLE;
                this.drawable = drawable;
                length = 1;
                s = " ";
            }

            private Element(Drawable drawable, int align, int type) {
                if (align != AlignImageSpan.ALIGN_BASELINE
                        && align != AlignImageSpan.ALIGN_BOTTOM && align != AlignImageSpan.ALIGN_CENTER
                        && align != AlignImageSpan.ALIGN_TOP) {
                    align = AlignImageSpan.ALIGN_CENTER;
                }
                this.type = TYPE_DRAWABLE_VERTICAL;
                this.drawable = drawable;
                this.align = align;
                length = 1;
                s = " ";
            }

            private Element(String s) {
                type = TYPE_STRING;
                this.s = s;
                length = s.length();
            }

            private Element(String s, int color) {
                type = TYPE_TINT_STRING;
                this.s = s;
                this.color = color;
                length = s.length();
            }

            private Element(String s, int color, float size) {
                type = TYPE_TINT_STRING;
                this.s = s;
                this.ratio = size;
                this.color = color;
                length = s.length();
            }

            private Element(String s, int color, View.OnClickListener listener) {
                type = TYPE_CLICKABLE_STRING;
                this.s = s;
                this.color = color;
                length = s.length();
                onClickListener = listener;
            }
        }

        private SpannableStringBuilder spannableString;
        private Context context;
        private List<Element> elements;
        private float height = -1;

        private boolean containsString = false;

        public Builder(Context context) {
            this.context = context;
            this.elements = new ArrayList<>();
        }

        public Builder(Context context, SpannableStringBuilder spannableString) {
            this.context = context;
            this.spannableString = spannableString;
            this.elements = new ArrayList<>();
        }

        public Builder setHeight(float height) {
            this.height = height;
            return this;
        }

        public Builder maybe(Callback<Builder> builderCallback) {
            builderCallback.onCall(this);
            return this;
        }

        public Builder compose(Callback<SpannableStringBuilder> call) {
            elements.add(new Element(call));
            return this;
        }

        public Builder addDrawable(Drawable drawable) {
            elements.add(new Element(drawable));
            return this;
        }


        public Builder addDrawable(Drawable drawable, int align) {
            elements.add(new Element(drawable, align));
            return this;
        }

        public Builder addDrawable(Drawable drawable, int align, int type) {
            elements.add(new Element(drawable, align, type));
            return this;
        }

        public Builder addDrawable(Drawable drawable, int align, View.OnClickListener... listener) {
            elements.add(new Element(drawable, align, listener));
            return this;
        }

        public Builder addString(String s) {
            elements.add(new Element(s));
            containsString = true;
            return this;
        }

        public Builder addString(String s, int color) {
            elements.add(new Element(s, color));
            containsString = true;
            return this;
        }

        public Builder addStringColorRes(String s, @ColorRes int colorRes) {
            elements.add(new Element(s, ContextCompat.getColor(context, colorRes)));
            containsString = true;
            return this;
        }

        public Builder addStringColorResAndRatio(String s, @ColorRes int colorRes, float ratio) {
            elements.add(new Element(s, ContextCompat.getColor(context, colorRes), ratio));
            containsString = true;
            return this;
        }

        public Builder addString(String s, int color, View.OnClickListener listener) {
            elements.add(new Element(s, color, listener));
            containsString = true;
            return this;
        }

        public Builder addStringColorRes(String s, @ColorRes int colorRes, View.OnClickListener listener) {
            elements.add(new Element(s, ContextCompat.getColor(context, colorRes), listener));
            containsString = true;
            return this;
        }

        public SpannableStringBuilder getSpannableString() {
            return spannableString;
        }

        public CharSequence build() {
            if (spannableString == null) {
                spannableString = new SpannableStringBuilder();
            }

            for (Element e : elements) {
                spannableString.append(e.s);
                if (e.type == Element.TYPE_COMPOSE) {
                    e.composeCall.onCall(spannableString);
                } else if (e.type == Element.TYPE_TINT_STRING) {
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(e.color);
                    spannableString.setSpan(colorSpan, spannableString.length() - e.length, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (e.ratio > 0) {
                        spannableString.setSpan(new RelativeSizeSpan(e.ratio), spannableString.length() - e.length, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else if (e.type == Element.TYPE_CLICKABLE_STRING) {
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            e.onClickListener.onClick(widget);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setColor(e.color);
                            ds.setUnderlineText(false);
                        }
                    };
                    spannableString.setSpan(clickableSpan, spannableString.length() - e.length, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (e.type == Element.TYPE_DRAWABLE) {

                    int width = e.drawable.getIntrinsicWidth();
                    int height = e.drawable.getIntrinsicHeight();

                    if (this.height > 0) {
                        float i = (float) this.height / height;
                        height = (int) (height * i);
                        width = (int) (width * i);
                    }

                    // TODO: 16/12/14 字体高度不等同于实际绘制文字的高度,可能会造成图片的偏移,要获取文字实际高度计算drawable高度？
                    e.drawable.setBounds(0, 0, width, height);
                    spannableString.append("\r");
                    ImageSpan span = new ImageSpan(e.drawable, containsString ? e.align : ImageSpan.ALIGN_BOTTOM);
                    spannableString.setSpan(span, spannableString.length() - e.length, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (e.onClickListener != null) {
                        ClickableSpan span1 = new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
                                e.onClickListener.onClick(widget);
                            }
                        };
                        spannableString.setSpan(span1, spannableString.length() - e.length, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else if (e.type == Element.TYPE_DRAWABLE_VERTICAL) {
                    //针对垂直居中
                    int width = e.drawable.getIntrinsicWidth();
                    int height = e.drawable.getIntrinsicHeight();

                    if (this.height > 0) {
                        float i = this.height / height;
                        height = (int) (height * i);
                        width = (int) (width * i);
                    }

                    e.drawable.setBounds(0, 0, width, height);
                    spannableString.append("\r");
                    AlignImageSpan span = new AlignImageSpan(e.drawable, e.align);
                    spannableString.setSpan(span, spannableString.length() - e.length, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (e.onClickListener != null) {
                        ClickableSpan span1 = new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
                                e.onClickListener.onClick(widget);
                            }
                        };
                        spannableString.setSpan(span1, spannableString.length() - e.length, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            return spannableString;
        }
    }
}
