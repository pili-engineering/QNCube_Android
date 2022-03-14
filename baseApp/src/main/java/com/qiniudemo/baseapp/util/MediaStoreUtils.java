package com.qiniudemo.baseapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaStoreUtils {


    public static final String TAG = MediaStoreUtils.class.getSimpleName();


    ///////////////////////////////////////////////////////////////////////////
    // 下面是对外公开的重载的方法
    ///////////////////////////////////////////////////////////////////////////

    public static void notifyScanDcim(Context context, String filePath) {
        scanFile(context, filePath);
    }




    public static void insertImageToMediaStore(Context context, String filePath, long createTime) {
        insertImageToMediaStore(context, filePath, createTime, 0, 0);
    }


    ///////////////////////////////////////////////////////////////////////////
    // 扫描系统相册核心方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 针对系统文夹只需要扫描,不用插入内容提供者,不然会重复
     *
     * @param context  上下文
     * @param filePath 文件路径
     */
    public static void scanFile(Context context, String filePath) {
        if (!checkFile(filePath))
            return;
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(intent);
    }


    ///////////////////////////////////////////////////////////////////////////
    // 非系统相册像MediaContent中插入数据，核心方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 针对非系统文件夹下的文件,使用该方法
     * 插入时初始化公共字段
     *
     * @param filePath 文件
     * @param time     ms
     * @return ContentValues
     */
    private static ContentValues initCommonContentValues(String filePath, long time) {
        ContentValues values = new ContentValues();
        File saveFile = new File(filePath);
        long timeMillis = getTimeWrap(time);
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, timeMillis);
        values.put(MediaStore.MediaColumns.DATE_ADDED, timeMillis);
        values.put(MediaStore.MediaColumns.DATA, saveFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, saveFile.length());
        return values;
    }

    /**
     * 保存到照片到本地，并插入MediaStore以保证相册可以查看到,这是更优化的方法，防止读取的照片获取不到宽高
     *
     * @param context    上下文
     * @param filePath   文件路径
     * @param createTime 创建时间 <=0时为当前时间 ms
     * @param width      宽度
     * @param height     高度
     */
    public static void insertImageToMediaStore(Context context, String filePath, long createTime, int width, int height) {
        if (!checkFile(filePath))
            return;
        createTime = getTimeWrap(createTime);
        ContentValues values = initCommonContentValues(filePath, createTime);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, createTime);
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (width > 0) values.put(MediaStore.Images.ImageColumns.WIDTH, 0);
            if (height > 0) values.put(MediaStore.Images.ImageColumns.HEIGHT, 0);
        }
        values.put(MediaStore.MediaColumns.MIME_TYPE, getPhotoMimeType(filePath));
        context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * 保存到视频到本地，并插入MediaStore以保证相册可以查看到,这是更优化的方法，防止读取的视频获取不到宽高
     *
     * @param context    上下文
     * @param filePath   文件路径
     */
    public static void insertVideoToMediaStore(Context context, String filePath) {
        if (!checkFile(filePath))
            return;

        ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
       //  values.put(MediaStore.Video.Media.DATA, filePath);
        // Add a new record (identified by uri) without the video, but with the values just set.
        Uri uri =context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        Log.d(TAG,"insertVideoToMediaStore");
        if(uri==null){
            return;
        }
        try {
            InputStream is = new FileInputStream(new File(filePath));
            OutputStream os = context.getContentResolver().openOutputStream(uri);
            byte[] buffer = new byte[4096]; // tweaking this number may increase performance
            int len;
            while ((len = is.read(buffer)) != -1){
                os.write(buffer, 0, len);
            }
            os.flush();
            is.close();
            os.close();
        } catch (Exception e) {
            Log.e(TAG, "exception while writing video: ", e);
        }

        context. sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }


    // 是不是系统相册
    private static boolean isSystemDcim(String path) {
        return path.toLowerCase().contains("dcim") || path.toLowerCase().contains("camera");
    }

    // 获取照片的mine_type
    private static String getPhotoMimeType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith("jpg") || lowerPath.endsWith("jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith("png")) {
            return "image/png";
        } else if (lowerPath.endsWith("gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }

    // 获取video的mine_type,暂时只支持mp4,3gp
    private static String getVideoMimeType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith("mp4") || lowerPath.endsWith("mpeg4")) {
            return "video/mp4";
        } else if (lowerPath.endsWith("3gp")) {
            return "video/3gp";
        }
        return "video/mp4";
    }

    // 获得转化后的时间
    private static long getTimeWrap(long time) {
        if (time <= 0) {
            return System.currentTimeMillis();
        }
        return time;
    }

    // 检测文件存在
    private static boolean checkFile(String filePath) {
        boolean result = true;
                File file =new File(filePath);
        result=  file.exists();
        Log.e(TAG, "文件不存在 path = " + filePath);
        return result;
    }

    public static int saveImageToGallery(Bitmap bmp,Context context) {
        //生成路径
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dirName = "erweima16";
        File appDir = new File(root , dirName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        //文件名为时间
        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sd = sdf.format(new Date(timeStamp));
        String fileName = sd + ".jpg";

        //获取文件
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            //通知系统相册刷新

            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(new File(file.getPath()))));
            return 2;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

}
