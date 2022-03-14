package com.qiniu.bzui.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;


public class EmojiManager {

    private  final String TAG = "EmojiManager";

    private  Context gContext;
    private  ArrayList<Integer> emojiCodeList = new ArrayList<>();
    private  ArrayList<Integer> emojiResourceList = new ArrayList<>();


    private static EmojiManager emojiManager = null;

    public static   EmojiManager  getInstance(Context context){
        if(emojiManager==null){
            emojiManager=new EmojiManager(context);
        }
        return emojiManager;
    }
    private EmojiManager(Context context) {

        gContext = context.getApplicationContext();
        Resources resources = gContext.getResources();
        int[] codes = resources.getIntArray(R.array.chatroom_emoji_code_list);
        TypedArray array = resources.obtainTypedArray(R.array.chatroom_emoji_res_list);
        if (codes.length != array.length()) {
            array.recycle();
            throw new IndexOutOfBoundsException("Code and resource are not match in Emoji xml.");
        }

        for (int i = 0; i < codes.length; i++) {
            emojiCodeList.add(codes[i]);
            emojiResourceList.add(array.getResourceId(i, -1));
        }
        array.recycle();
    }

    public  int getSize() {
        return emojiCodeList.size();
    }

    public  int getCode(int position) {
        return emojiCodeList.get(position);
    }

    public  List<Integer> getResourceList(int start, int count) {
        return new ArrayList<>(emojiResourceList.subList(start, start + count));
    }

    private  int getResourceByCode(int code) throws Resources.NotFoundException {
        for (int i = 0; i < emojiCodeList.size(); i++) {
            if (emojiCodeList.get(i) == code) {
                return emojiResourceList.get(i);
            }
        }
        throw new Resources.NotFoundException("Unsupported emoji code <" + code + ">, which is not in Emoji list.");
    }

    public  CharSequence parse(String text, int textSize) {
        if (text == null) {
            return "";
        }

        final char[] chars = text.toCharArray();
        final SpannableStringBuilder ssb = new SpannableStringBuilder(text);

        int codePoint;
        boolean isSurrogatePair;
        for (int i = 0; i < chars.length; i++) {
            if (Character.isHighSurrogate(chars[i])) {
                continue;
            } else if (Character.isLowSurrogate(chars[i])) {
                if (i > 0 && Character.isSurrogatePair(chars[i - 1], chars[i])) {
                    codePoint = Character.toCodePoint(chars[i - 1], chars[i]);
                    isSurrogatePair = true;
                } else {
                    continue;
                }
            } else {
                codePoint = (int) chars[i];
                isSurrogatePair = false;
            }

            if (emojiCodeList.contains(codePoint)) {
                Bitmap bitmap = BitmapFactory.decodeResource(gContext.getResources(), getResourceByCode(codePoint));
                BitmapDrawable bmpDrawable = new BitmapDrawable(gContext.getResources(), bitmap);
                bmpDrawable.setBounds(0, 0, (int) textSize, (int) textSize);
                CenterImageSpan imageSpan = new CenterImageSpan(bmpDrawable);
                ssb.setSpan(imageSpan, isSurrogatePair ? i - 1 : i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return ssb;
    }

    private  class CenterImageSpan extends ImageSpan {

        public CenterImageSpan(Drawable draw) {
            super(draw);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x,
                         int top, int y, int bottom, Paint paint) {
            Drawable b = getDrawable();
            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int transY = ((bottom - top) - getDrawable().getBounds().bottom) / 2 + top;
            canvas.save();
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }
}
