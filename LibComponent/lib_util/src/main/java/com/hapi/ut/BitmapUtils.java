/*
 * Copyright (c) 2016  athou（cai353974361@163.com）.
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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.hapi.ut.constans.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BitmapUtils {

    public static final int IMAGE_RESOLUTION_BIG_WIDTH = 720;// 输出图片大小
    public static final int IMAGE_RESOLUTION_BIG_HEIGHT = 720;// 输出图片大小

    public static final int IMAGE_RESOLUTION_SMALL_WIDTH = 120;// 输出图片大小
    public static final int IMAGE_RESOLUTION_SMALL_HEIGHT = 120;// 输出图片大小

    /**
     * 根据图片名称，获取bitmap
     *
     * @param name
     * @return
     * @since caicai
     */
    public static Bitmap getBitmapFromRes(Context context, String name) {
        int resID = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return BitmapFactory.decodeResource(context.getResources(), resID);
    }

    /**
     * 通过图片名称找到对应图片资源ID
     *
     * @param c
     * @param pic
     * @return
     * @author 菜菜
     */
    public static int getImageResIdByDrawableName(Context c, String pic) {
        if (StrUtil.isEmpty(pic)) {
            return -1;
        }
        Class draw = null;
        try {
            draw = Class.forName(c.getPackageName() + ".R$drawable");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        if (draw == null) {
            return -1;
        }
        try {
            Field field = draw.getDeclaredField(pic);
            return field.getInt(pic);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取指定宽高比的尺寸（以屏幕宽度为基准）
     *
     * @param scale 高宽比（9:16 or 3:4）
     * @return
     */
    public static Rect getDefaultImageBounds(float scale) {
        DisplayMetrics display = Resources.getSystem().getDisplayMetrics();
        int width = display.widthPixels;
        int height = (int) (width * scale);

        Rect bounds = new Rect(0, 0, width, height);
        return bounds;
    }

    /**
     * drawable转bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * bitmap转Drawable
     *
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        return new BitmapDrawable(Resources.getSystem(), bitmap);
    }

    /**
     * 获取照片，未压缩，可能OOM
     *
     * @param uri 图片路径uri
     * @return bitmap
     */
    public static Bitmap decodeUriAsBitmap(Context context, Uri uri) {
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /**
     * 获取照片，未压缩，可能OOM
     *
     * @param file 图片file
     * @return bitmap
     */
    public static Bitmap decodeFileAsBitmap(File file) {
        Bitmap bitmap = null;
        if (file != null && file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(fis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 获取照片，未压缩，可能OOM
     *
     * @param filePath 图片filePath
     * @return bitmap
     */
    public static Bitmap decodePathAsBitmap(String filePath) {
        Bitmap bitmap = null;
        if (!StrUtil.isEmpty(filePath)) {
            bitmap = BitmapFactory.decodeFile(filePath);
        }
        return bitmap;
    }

    /**
     * 解析本地视频文件，获取缩略图，时长，大小
     *
     * @param context
     * @param videoPath
     * @return
     */
    public static Map<String, Object> parseVideo(Context context, String videoPath) {
        if (!DeviceUtil.isCanUseSD() || StrUtil.isEmpty(videoPath)) {
            return null;
        }
        File file = new File(videoPath);
        if (!file.exists()) {
            return null;
        }
        Map<String, Object> map = null;
        MediaMetadataRetriever media = null;
        try {
            media = new MediaMetadataRetriever();
            media.setDataSource(context, Uri.fromFile(file));
            //取得指定时间（第6us）的Bitmap，即可以实现抓图（缩略图）功能
            Bitmap bitmap = media.getFrameAtTime(6);
            long video_length = Long.parseLong(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            long video_size = FileUtil.getFileSize(file);

            map = new HashMap<>();
            map.put("thumbnail", bitmap);
            map.put("duration", video_length);
            map.put("size", video_size);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (media != null) {
                try {
                    media.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    /**
     * 根据Uri获取文件path
     *
     * @param contentUri
     * @return
     */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String fileRealPath = null;
        if (contentUri != null) {
            String imgUriPath = contentUri.getPath();
            Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                // FIXME column '_data' does not exist
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                fileRealPath = cursor.getString(columnIndex);
            } else {
                fileRealPath = imgUriPath;
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileRealPath;
    }

    /**
     * 旋转图片
     *
     * @param path
     * @return
     */
    public static String ratateImage(String path) {
        int degree = getBitmapDegree(path);
        if (degree != 0) {// 旋转照片角度
            Bitmap bmp = decodePathAsBitmap(path);
            if (bmp == null) {
                return null;
            }
            bmp = rotateBitmapByDegree(bmp, degree);

            saveBitmap2JPG(bmp, path, 90);

            recycle(bmp);
        }
        return path;
    }

    /**
     * 压缩图片以及质量同时拍照角度旋转
     */
    public static String compressRotateImage(String srcPath, String destPath) {
        if (StrUtil.isEmpty(srcPath) || StrUtil.isEmpty(destPath)) {
            Log.e("BitmapUtils", "Can not compress a image with null path.");
            return null;
        }
        File file = new File(srcPath);
        if (!file.exists()) {
            Log.e("BitmapUtils", "Can not compress a image can not found.");
            return null;
        }
        return compressRotateImage(srcPath, destPath, getFitQualitySize((int) (file.length() / Constants.KB)));
    }

    /**
     * 压缩图片以及质量同时拍照角度旋转，只按宽度压缩大小
     */
    public static String compressRotateImage(String srcPath, String destPath, int q) {
        return compressImage(srcPath, destPath, BitmapUtils.IMAGE_RESOLUTION_BIG_WIDTH, 0, q, true);
    }

    /**
     * 压缩图片以及质量，注：只按宽度压缩比例
     *
     * @param srcPath  the whole source image path
     * @param destPath the whole destination image path
     * @param isRotate whether rotate the image
     * @return
     */
    public static String compressImage(String srcPath, String destPath, boolean isRotate) {
        if (StrUtil.isEmpty(srcPath) || StrUtil.isEmpty(destPath)) {
            Log.e("BitmapUtils", "Can not compress a image with null path.");
            return null;
        }
        File file = new File(srcPath);
        if (!file.exists()) {
            Log.e("BitmapUtils", "Can not compress a image can not found.");
            return null;
        }
        return compressImage(srcPath, destPath, BitmapUtils.IMAGE_RESOLUTION_BIG_WIDTH, 0, getFitQualitySize((int) (file.length() / Constants.KB)), isRotate);
    }


    /**
     * Compress image to allocated width and height.
     *
     * @param srcPath  the whole source image path
     * @param destPath the whole destination image path
     * @param width    destination width, if width = 0 means only compress by height.
     * @param height   destination height, if height = 0 means only compress by width.
     * @param quality  compress image with the quality
     * @param isRotate whether rotate the image
     * @return
     */
    public static String compressImage(String srcPath, String destPath, int width, int height, int quality, boolean isRotate) {
        if (StrUtil.isEmpty(srcPath) || StrUtil.isEmpty(destPath)) {
            Log.e("BitmapUtils", "Can not compress a image with null path.");
            return null;
        }

        Bitmap bm = compressImageBySize(srcPath, width, height);

        if (bm != null && isRotate) {
            int degree = getBitmapDegree(srcPath);
            if (degree != 0) {// 旋转照片角度
                bm = rotateBitmapByDegree(bm, degree);
            }
        }

        if (bm != null) {
            Log.d("BitmapUtils", "image size: compress - olderSize:" + new File(srcPath).length() / Constants.KB + " kb");
            File newFile = saveBitmap2JPG(bm, destPath, quality);
            Log.d("BitmapUtils", "image size: compress - newSize:" + new File(destPath).length() / Constants.KB + "  kb");
            recycle(bm);
            return newFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Save bitmap to dest path.
     *
     * @param bitmap
     * @param destPath
     */
    public static File saveBitmap2JPG(Bitmap bitmap, String destPath) {
        return saveBitmap2JPG(bitmap, destPath, 100);
    }

    /**
     * Save bitmap to dest path.
     *
     * @param bitmap
     * @param destPath
     * @param quality
     */
    public static File saveBitmap2JPG(Bitmap bitmap, String destPath, int quality) {
        File newFile = FileUtil.createFile(destPath);
        if (newFile == null || bitmap == null) {
            return null;
        }
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
            outStream.flush();
            Log.e("BitmapUtils", "image size: save - " + newFile.length() / Constants.KB + " kb");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return newFile;
    }

    /**
     * Get scaled Bitmap.
     *
     * @param srcPath the whole source image path
     * @param width   destination width, if width = 0 means only compress by height.
     * @param height  destination height, if height = 0 means only compress by width.
     * @return
     */
    public static Bitmap compressImageBySize(String srcPath, int width, int height) {
        if (StrUtil.isEmpty(srcPath)) {
            return null;
        }
        Log.d("BitmapUtils", "image size: compress before - " + new File(srcPath).length() / Constants.KB + " kb");

        final Options options = new Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);
        if (options.outWidth != 0 && options.outHeight != 0) {
            int srcWidth = width != 0 ? options.outWidth / width : 0;
            int srcHeight = height != 0 ? options.outHeight / height : 0;
            if (srcWidth > srcHeight) {
                options.inSampleSize = srcWidth;
            } else {
                options.inSampleSize = srcHeight;
            }
            if (options.inSampleSize == 0) {
                options.inSampleSize = 1;
            } else if (options.inSampleSize % 2 != 0 && options.inSampleSize > 1) {
                options.inSampleSize = options.inSampleSize - 1;
            }

            if (options.inSampleSize == 1) {
                // Not compress...
                return decodePathAsBitmap(srcPath);
            }
            options.inJustDecodeBounds = false;
            options.inInputShareable = true;
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeFile(srcPath, options);
            return bitmap;
        } else {
            return null;
        }
    }

    /**
     * Get the quality with the given file size with KB.
     *
     * @param fileSize size by KB
     * @return the fit quality
     */
    public static int getFitQualitySize(int fileSize) {
        if (fileSize < 400) {
            return 100;
        } else if (fileSize < 1000) {
            return 92;
        } else if (fileSize < 1600) {
            return 84;
        } else if (fileSize < 2200) {
            return 76;
        } else if (fileSize < 2800) {
            return 68;
        } else if (fileSize < 3400) {
            return 60;
        } else if (fileSize < 4000) {
            return 52;
        } else if (fileSize < 6000) {
            return 44;
        } else if (fileSize < 8000) {
            return 36;
        } else if (fileSize < 12000) {
            return 28;
        } else {
            return 20;
        }
    }

    /**
     * 压缩bitmap的质量
     *
     * @param bitmap
     * @param quality
     * @return
     */
    public static Bitmap compressImageByQuality(Bitmap bitmap, int quality) {
        Bitmap bm = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
            bm = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bmp    需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bmp, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bmp;
        }
        return returnBm;
    }

    public static Bitmap rotateBitmapByDegree2(Bitmap bm, int degree) {
        try {
            Matrix m = new Matrix();
            m.setRotate(degree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            float targetX, targetY;
            if (degree == 90) {
                targetX = bm.getHeight();
                targetY = 0;
            } else {
                targetX = bm.getHeight();
                targetY = bm.getWidth();
            }

            final float[] values = new float[9];
            m.getValues(values);

            float x1 = values[Matrix.MTRANS_X];
            float y1 = values[Matrix.MTRANS_Y];

            m.postTranslate(targetX - x1, targetY - y1);

            Bitmap returnBm = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
            Paint paint = new Paint();
            Canvas canvas = new Canvas(returnBm);
            canvas.drawBitmap(bm, m, paint);

            if (returnBm == null) {
                returnBm = bm;
            }
            return returnBm;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }

    /**
     * 图片转灰度
     */
    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        int width, height;
        height = bmSrc.getHeight();
        width = bmSrc.getWidth();
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }

    /**
     * 为图片着色
     *
     * @param drawable
     * @param colors
     * @return
     */
    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    /**
     * 回收Bitmap
     *
     * @param bm
     */
    public static void recycle(Bitmap bm) {
        try {
            if (bm != null && !bm.isRecycled()) {
                bm.recycle();
                bm = null;
            }
        } catch (Exception ignore) {

        }
    }
}
