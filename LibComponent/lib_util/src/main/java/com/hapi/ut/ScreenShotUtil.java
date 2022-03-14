/*
 * Copyright (c) 2017  athou（cai353974361@163.com）.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hapi.ut;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.collection.LruCache;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.View.MeasureSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xx
 * @date 2017/1/12
 */

public class ScreenShotUtil {

    private int cacheBitmapKey = 0x101;
    private int cacheBitmapDirtyKey = 0x012;

    private Bitmap.Config bitmap_quality = Bitmap.Config.RGB_565;

    private boolean quick_cache = false;
    private int color_background = Color.parseColor("#ff000000");

    public Bitmap getMagicDrawingCache(View view) {
        cacheBitmapKey = view.getId();
        cacheBitmapDirtyKey = cacheBitmapKey + 1;

        Bitmap bitmap = (Bitmap) view.getTag(cacheBitmapKey);
        Boolean dirty = (Boolean) view.getTag(cacheBitmapDirtyKey);
        if (view.getWidth() + view.getHeight() == 0) {
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        if (bitmap == null || bitmap.getWidth() != viewWidth || bitmap.getHeight() != viewHeight) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            bitmap = Bitmap.createBitmap(viewWidth, viewHeight, bitmap_quality);
            view.setTag(cacheBitmapKey, bitmap);
            dirty = true;
        }
        if (dirty == true || !quick_cache) {
            bitmap.eraseColor(color_background);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            view.setTag(cacheBitmapDirtyKey, false);
        }
        return bitmap;
    }

    /**
     * 针对通过java代码生成一个view，获取生成的图片
     *
     * @param view
     * @param width
     * @param height
     * @return
     */
    public static Bitmap canvasBitmap(View view, int width, int height) {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, width, height);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 截屏方法
     *
     * @param view
     */
    public static Bitmap takeScreenShot(View view) {
        /**
         * 我们要获取它的cache先要通过setDrawingCacheEnable方法把cache开启，
         * 然后再调用getDrawingCache方法就可以获得view的cache图片了。
         * buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，
         * 若果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。 若果要更新cache,
         * 必须要调用destoryDrawingCache方法把旧的cache销毁， 才能建立新的。
         */
//        view.setDrawingCacheEnabled(true);// 开启获取缓存
        view.buildDrawingCache();
        // 得到View的cache
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * NestedScrollView
     */
    public static Bitmap shotScrollView(NestedScrollView scrollView) {
        int h = 0;
        Bitmap bitmap = null;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
            scrollView.getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
        }
        //scrollView.getWidth()
        bitmap = Bitmap.createBitmap(ScreenUtil.getScreenWidth(), h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }

    /**
     * recyclerview长截图
     * https://gist.github.com/PrashamTrivedi/809d2541776c8c141d9a
     */
    public static Bitmap shotRecyclerView(RecyclerView view) {
        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(
                        MeasureSpec.makeMeasureSpec(view.getWidth(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(),
                        holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {

                    bitmaCache.put(String.valueOf(i), drawingCache);
                }
                height += holder.itemView.getMeasuredHeight();
            }

            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            Drawable lBackground = view.getBackground();
            if (lBackground instanceof ColorDrawable) {
                ColorDrawable lColorDrawable = (ColorDrawable) lBackground;
                int lColor = lColorDrawable.getColor();
                bigCanvas.drawColor(lColor);
            }

            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmaCache.get(String.valueOf(i));
                bigCanvas.drawBitmap(bitmap, 0f, iHeight, paint);
                iHeight += bitmap.getHeight();
                bitmap.recycle();
            }
        }
        return bigBitmap;
    }

    /**
     * 截屏方法
     *
     * @param view
     * @param dir
     */
    public static String takeScreenShot(View view, String dir) {
        Bitmap bitmap = takeScreenShot(view);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        SimpleDateFormat simple = new SimpleDateFormat("yyyyMMddhhmmss");
        String time = simple.format(new Date());
        canvas.save();
        canvas.restore();

        FileOutputStream fos = null;
        try {
            File file = new File(dir, "screen_shot_" + time + ".jpg");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            fos = new FileOutputStream(file);
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
            view.setDrawingCacheEnabled(false);
            view.destroyDrawingCache();
        }
        return null;
    }

    /**
     * 截屏方法
     *
     * @param view
     * @param path
     */
    public static String takeScreenShot2(View view, String path) {
        Bitmap bitmap = takeScreenShot(view);
        Canvas canvas = new Canvas(bitmap);

        canvas.save();
        canvas.restore();

        FileOutputStream fos = null;
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            fos = new FileOutputStream(file);
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
            view.setDrawingCacheEnabled(false);
            view.destroyDrawingCache();
        }
        return null;
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        try {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap tabBg = view.getDrawingCache();
            Rect frame = new Rect();
            activity.getWindow().getDecorView()
                    .getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;

            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            int height = Resources.getSystem().getDisplayMetrics().heightPixels;
            tabBg = Bitmap.createBitmap(tabBg, 0, statusBarHeight, width,
                    height - statusBarHeight);
            view.destroyDrawingCache();
            return tabBg;
        } catch (Exception ignore) {
        }
        return null;
    }
}
