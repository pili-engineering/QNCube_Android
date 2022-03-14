package com.hapi.ut;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述：文件操作类.
 *
 * @author xx
 */
public class FileUtil {

    /**
     * 文件是否存在
     *
     * @param path
     * @return
     */
    public static boolean exists(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    /**
     * 通过文件路径获取文件名
     *
     * @param path
     * @return
     */
    public static String getFileNameByPath(@NonNull String path) {
        int start = path.lastIndexOf("/");
        if (start != -1) {
            return path.substring(start, path.length());
        } else {
            return null;
        }
    }

    /**
     * 获取文件夹大小
     *
     * @param dirPath
     * @return
     */
    public static long getDirSize(@NonNull String dirPath) {
        return getDirSize(new File(dirPath));
    }

    /**
     * 获取文件夹大小
     *
     * @param dirFile
     * @return
     */
    public static long getDirSize(@Nullable File dirFile) {
        if (dirFile == null || !dirFile.exists()) {
            return 0;
        }
        long size = 0;
        File flist[] = dirFile.listFiles();
        if (flist == null) {
            return 0;
        }
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getDirSize(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return
     */
    public static long getFileSize(@NonNull String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return 0;
        }
        return getFileSize(new File(filePath));
    }

    /**
     * 获取文件大小
     *
     * @param file
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public static long getFileSize(@Nullable File file) {
        if (file == null || !file.exists()) {
            Log.e("FileUtil", "文件不存在!");
            return 0;
        }
        if (!file.canRead()) {
            file.setReadable(true);
        }
        long size = 0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            size = fis.available();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fis);
        }
        return size;
    }

    /**
     * 描述：读取Assets目录的文件内容
     *
     * @param context
     * @param name
     * @return
     * @throws
     */
    @Nullable
    public static String readAssetsByName(@NonNull Context context, @NonNull String name, @NonNull String encoding) {
        String text = null;
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            inputReader = new InputStreamReader(context.getAssets().open(name));
            bufReader = new BufferedReader(inputReader);
            String line = null;
            StringBuffer buffer = new StringBuffer();
            while ((line = bufReader.readLine()) != null) {
                buffer.append(line);
            }
            text = new String(buffer.toString().getBytes(), encoding);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(bufReader, inputReader);
        }
        return text;
    }

    /**
     * 描述：读取Raw目录的文件内容
     *
     * @param context
     * @param id
     * @return
     * @throws
     */
    @Nullable
    public static String readRawByName(@NonNull Context context, int id, @NonNull String encoding) {
        String text = null;
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            inputReader = new InputStreamReader(context.getResources().openRawResource(id));
            bufReader = new BufferedReader(inputReader);
            String line = null;
            StringBuffer buffer = new StringBuffer();
            while ((line = bufReader.readLine()) != null) {
                buffer.append(line);
            }
            text = new String(buffer.toString().getBytes(), encoding);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(bufReader, inputReader);
        }
        return text;
    }

    /**
     * 获取app内文件
     * 　src/images/test01.jpg
     *
     * @param cls      images/同级class
     * @param filename test01.jpg
     * @return
     */
    public static File getAppFile(@NonNull Class cls, @NonNull String filename) {
        if (cls == null || TextUtils.isEmpty(filename)) {
            return null;
        }
        return new File(cls.getResource(filename).getFile());
    }

    /**
     * 打开文件
     *
     * @param dir
     * @param filename
     * @param create   true
     * @return null if the file open failed
     */
    public static File openFile(@NonNull String dir, @NonNull String filename, boolean create) {
        if (!DeviceUtil.isCanUseSD() || TextUtils.isEmpty(dir) || TextUtils.isEmpty(filename)) {
            return null;
        }
        File file = new File(dir);
        if (!file.exists()) {
            if (create) {
                file.mkdirs();
            } else {
                return null;
            }
        }
        file = new File(dir, filename);
        if (!file.exists()) {
            if (create) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return null;
            }
        }
        return file;
    }

    /**
     * 打开文件
     *
     * @param path
     * @param create true
     * @return null if the file open failed
     */
    public static File openFile(@NonNull String path, boolean create) {
        if (!DeviceUtil.isCanUseSD() || TextUtils.isEmpty(path)) {
            return null;
        }
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                if (create) {
                    file.createNewFile();
                } else { //文件不存在，且不创建则返回为null
                    return null;
                }
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建文件
     *
     * @param dir
     * @param filename
     * @return
     */
    @Nullable
    public static File createFile(@NonNull String dir, String filename) {
        File file = null;
        if (DeviceUtil.isCanUseSD() && !TextUtils.isEmpty(dir) && !TextUtils.isEmpty(filename)) {
            file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(dir, filename);
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 创建文件
     *
     * @param path
     * @return
     */
    @Nullable
    public static File createFile(@NonNull String path) {
        File file = null;
        if (DeviceUtil.isCanUseSD() && !TextUtils.isEmpty(path)) {
            try {
                file = new File(path);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 删除文件夹下所有文件，文件夹一并删除
     */
    public static void deleteDir(File file) {
        deleteDir(file, true);
    }

    /**
     * 删除文件夹下所有文件
     *
     * @param file    要删除的文件夹
     * @param delRoot ture:删除根目录   false:保留根目录
     */
    public static void deleteDir(@Nullable File file, boolean delRoot) {
        if (file == null) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                deleteDir(childFiles[i]);
            }
            if (delRoot) {
                file.delete();
            }
        }
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @return true 删除成功; false 删除失败
     */
    public static boolean deleteFile(@NonNull String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件
     *
     * @param dir
     * @param filename
     * @return true 删除成功; false 删除失败
     */
    public static boolean deleteFile(@NonNull String dir, @NonNull String filename) {
        if (TextUtils.isEmpty(dir) || TextUtils.isEmpty(filename)) {
            return false;
        }
        File file = new File(dir, filename);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 描述：从sd卡中的文件读取到byte[].
     *
     * @param path sd卡中文件路径
     * @return byte[]
     */
    public static byte[] getDataFromFile(@NonNull String path) {
        if (!DeviceUtil.isCanUseSD() || TextUtils.isEmpty(path)) {
            return null;
        }
        byte[] bytes = null;
        ByteArrayOutputStream out = null;
        try {
            File file = new File(path);
            // 文件是否存在
            if (!file.exists()) {
                return null;
            }
            FileInputStream in = new FileInputStream(path);
            out = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
            }
            in.close();
            bytes = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(out);
        }
        return bytes;
    }

    /**
     * 描述：将byte数组写入文件.
     *
     * @param data the data
     * @param path the path
     */
    public static boolean saveDataToFile(@NonNull byte[] data, @NonNull String path) {
        return saveDataToFile(new String(data), new File(path), false);
    }

    /**
     * 描述：将byte数组写入文件.
     *
     * @param data   the data
     * @param path   the path
     * @param append true 表示追加  false 表示覆盖
     */
    public static boolean saveDataToFile(@NonNull byte[] data, @NonNull String path, boolean append) {
        return saveDataToFile(new String(data), new File(path), append);
    }

    /**
     * 描述：将byte数组写入文件.
     *
     * @param data the data
     * @param path the path
     */
    public static boolean saveDataToFile(@NonNull String data, @NonNull String path) {
        return saveDataToFile(new String(data), new File(path), false);
    }

    /**
     * 将字符串写入文件
     *
     * @param data
     * @param file
     * @return
     */
    public static boolean saveDataToFile(@NonNull String data, @NonNull File file, boolean append) {
        if (!DeviceUtil.isCanUseSD()) {
            return false;
        }
        FileWriter writer = null;
        try {
            // 文件是否存在
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file, append);
            writer.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(writer);
        }
        return false;
    }

    /**
     * 将InputStream写入文件
     *
     * @param is
     * @param file
     * @return
     */
    public static boolean saveDataToFile(@NonNull InputStream is, @NonNull File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            return saveDataToFile(fos, is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fos);
        }
        return false;
    }

    /**
     * 将数据流InputStream写入文件
     *
     * @param fos
     * @param is
     * @throws IOException
     */
    public static boolean saveDataToFile(@NonNull FileOutputStream fos, @NonNull InputStream is) throws IOException {
        int maxLength = 1024;
        byte[] buffer = new byte[maxLength];
        int len = 0;
        while ((len = is.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.flush();
        close(fos);
        return true;
    }

    /**
     * 更改文件到指定路径 和 名称
     *
     * @param oldFile
     * @param newPath
     * @return
     */
    public static boolean changeFileNameAndPath(@NonNull File oldFile, @NonNull String newPath) {
        File newNameFile = new File(newPath);
        if (oldFile.renameTo(newNameFile)) {
            Log.i("FileUtil", "succeed ==> oldFile:" + oldFile.getPath() + "\n newPath:" + newPath);
            return true;
        } else {
            Log.i("FileUtil", "failure ==> oldFile:" + oldFile.getPath() + "\n newPath:" + newPath);
            return false;
        }
    }

    /**
     * 复制文件
     */
    public static boolean copyFile(@NonNull File sourceFile, @NonNull File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            // 缓冲数组
            byte[] b = new byte[1024 * 1024 * 2];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(inBuff, outBuff);
        }
        return false;
    }

    /**
     * 获取文件名，外链模式和通过网络获取. <br/>
     * 需要在线程中
     *
     * @param url 文件地址
     * @return 文件名
     */
    public static String getFileNameFromUrl(@NonNull String url) {
        if (Thread.currentThread().getName().equals("main")) { //如果是主线程直接抛异常
            throw new RuntimeException("you must not use this method in mainthread");
        }
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String name = null;
        try {
            String suffix = null;
            // 获取后缀
            if (url.lastIndexOf(".") != -1) {
                suffix = url.substring(url.lastIndexOf("."));
                if (suffix.indexOf("/") != -1 || suffix.indexOf("?") != -1 || suffix.indexOf("&") != -1) {
                    suffix = null;
                }
                if (suffix == null) {
                    // 获取后缀
                    name = getRealFileNameFromUrl(url);
                } else {
                    name = url.substring(url.lastIndexOf("/"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 获取文件名，通过网络获取.
     *
     * @param url 文件地址
     * @return 文件名
     */
    @Nullable
    @WorkerThread
    public static String getRealFileNameFromUrl(String url) {
        String name = null;
        try {
            if (TextUtils.isEmpty(url)) {
                return name;
            }

            URL mUrl = new URL(url);
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) mUrl.openConnection();
            mHttpURLConnection.setConnectTimeout(5 * 1000);
            mHttpURLConnection.setRequestMethod("GET");
            mHttpURLConnection
                    .setRequestProperty(
                            "Accept",
                            "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
            mHttpURLConnection.setRequestProperty("Referer", url);
            mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
            mHttpURLConnection
                    .setRequestProperty(
                            "User-Agent",
                            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            mHttpURLConnection.connect();
            if (mHttpURLConnection.getResponseCode() == 200) {
                for (int i = 0; ; i++) {
                    String mine = mHttpURLConnection.getHeaderField(i);
                    if (mine == null) {
                        break;
                    }
                    if ("content-disposition".equals(mHttpURLConnection.getHeaderFieldKey(i).toLowerCase())) {
                        Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                        if (m.find()) {
                            return m.group(1).replace("\"", "");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 描述：获取网络文件的大小.
     *
     * @param Url 图片的网络路径
     * @return int 网络文件的大小
     */
    public static int getContentLengthFromUrl(String Url) {
        int mContentLength = 0;
        try {
            URL url = new URL(Url);
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) url.openConnection();
            mHttpURLConnection.setConnectTimeout(5 * 1000);
            mHttpURLConnection.setRequestMethod("GET");
            mHttpURLConnection
                    .setRequestProperty(
                            "Accept",
                            "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
            mHttpURLConnection.setRequestProperty("Referer", Url);
            mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
            mHttpURLConnection
                    .setRequestProperty(
                            "User-Agent",
                            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            mHttpURLConnection.connect();
            if (mHttpURLConnection.getResponseCode() == 200) {
                // 根据响应获取文件大小
                mContentLength = mHttpURLConnection.getContentLength();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("FileUtil", "获取长度异常：" + e.getMessage());
        }
        return mContentLength;
    }

    /**
     * 根据文件的最后修改时间进行排序
     */
    public static class FileLastModifSort implements Comparator<File> {
        @Override
        public int compare(@NonNull File arg0, @NonNull File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    /**
     * 复制assets文件到sd卡
     *
     * @param context
     * @param assetsFileName
     * @param targetOutFilePath
     */
    public static void copyAssetsFileToSD(Context context, String assetsFileName, String targetOutFilePath) {
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            File file = new File(targetOutFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            } else {
                return;
            }
            myOutput = new FileOutputStream(targetOutFilePath);
            myInput = context.getAssets().open(assetsFileName);
            byte[] buffer = new byte[1024 * 100];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(myInput, myOutput);
        }
    }

    /**
     * 获取缓存目录
     *
     * @param context
     * @return
     */
    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    /**
     * 关闭流
     *
     * @param closeables Closeable
     */
    @SuppressWarnings("WeakerAccess")
    public static void close(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
