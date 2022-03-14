package com.niucube.compui.beauty.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import com.niucube.compui.beauty.Constants;
import com.niucube.compui.beauty.R;
import com.niucube.compui.beauty.model.FilterItem;
import com.niucube.compui.beauty.model.MakeupItem;
import com.niucube.compui.beauty.model.StickerItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileUtils {

    public static boolean copyFileIfNeed(Context context, String fileName) {
        String path = getFilePath(context, fileName);
        if (path != null) {
            File file = new File(path);
            if (!file.exists()) {
                //如果模型文件不存在
                try {
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    InputStream in = context.getApplicationContext().getAssets().open(fileName);
                    if (in == null) {
                        Log.e("copyMode", "the src is not existed");
                        return false;
                    }
                    OutputStream out = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = in.read(buffer)) > 0) {
                        out.write(buffer, 0, n);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    file.delete();
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean copyFileIfNeed(Context context, String fileName, String className) {
        String path = getFilePath(context, className + File.separator + fileName);
        if (path != null) {
            File file = new File(path);
            if (!file.exists()) {
                //如果模型文件不存在
                try {
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    InputStream in = context.getAssets().open(className + File.separator + fileName);
                    if (in == null) {
                        Log.e("copyMode", "the src is not existed");
                        return false;
                    }
                    OutputStream out = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = in.read(buffer)) > 0) {
                        out.write(buffer, 0, n);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    file.delete();
                    return false;
                }
            }
        }
        return true;
    }

    public static String getFilePath(Context context, String fileName) {
        String path = null;
        File dataDir = context.getApplicationContext().getExternalFilesDir(null);
        if (dataDir != null) {
            path = dataDir.getAbsolutePath() + File.separator + fileName;
        }
        return path;
    }

    public static void copyStickerFiles(Context context, String index) {
        copyStickerZipFiles(context, index);
        copyStickerIconFiles(context, index);
    }

    public static void copyFilterFiles(Context context, String index) {
        copyFilterModelFiles(context, index);
        copyFilterIconFiles(context, index);
    }

    public static ArrayList<StickerItem> getStickerFiles(Context context, String index) {
        ArrayList<StickerItem> stickerFiles = new ArrayList<StickerItem>();
        Bitmap iconNone = BitmapFactory.decodeResource(context.getResources(), R.drawable.none);
        List<String> stickerModels = getStickerZipFilesFromSd(context, index);
        Map<String, Bitmap> stickerIcons = getStickerIconFilesFromSd(context, index);
        List<String> stickerNames = getStickerNames(context, index);
        for (int i = 0; i < stickerModels.size(); i++) {
            if (stickerIcons.get(stickerNames.get(i)) != null) {
                stickerFiles.add(new StickerItem(stickerNames.get(i), stickerIcons.get(stickerNames.get(i)), stickerModels.get(i)));
            } else {
                stickerFiles.add(new StickerItem(stickerNames.get(i), iconNone, stickerModels.get(i)));
            }
        }
        return stickerFiles;
    }

    public static List<String> copyStickerZipFiles(Context context, String className) {
        String[] files = null;
        ArrayList<String> modelFiles = new ArrayList<String>();
        try {
            files = context.getAssets().list(className);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;
            File folder = new File(folderpath);
            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        for (int i = 0; i < files.length; i++) {
            String str = files[i];
            if (str.indexOf(".zip") != -1 || str.indexOf(".model") != -1) {
                copyFileIfNeed(context, str, className);
            }
        }
        File file = new File(folderpath);
        File[] subFile = file.listFiles();
        if (subFile == null || subFile.length == 0) {
            return modelFiles;
        }
        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".zip") || filename.trim().toLowerCase().endsWith(".model")) {
                    modelFiles.add(filename);
                }
            }
        }
        return modelFiles;
    }

    public static List<String> getStickerZipFilesFromSd(Context context, String className) {
        ArrayList<String> modelFiles = new ArrayList<String>();
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;
            File folder = new File(folderpath);
            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        File file = new File(folderpath);
        File[] subFile = file.listFiles();
        if (subFile == null || subFile.length == 0) {
            return modelFiles;
        }
        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".zip") || filename.trim().toLowerCase().endsWith(".model")) {
                    modelFiles.add(filename);
                }
            }
        }
        return modelFiles;
    }

    public static Map<String, Bitmap> copyStickerIconFiles(Context context, String className) {
        String[] files = null;
        TreeMap<String, Bitmap> iconFiles = new TreeMap<String, Bitmap>();
        try {
            files = context.getAssets().list(className);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;

            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        for (int i = 0; i < files.length; i++) {
            String str = files[i];
            if (str.indexOf(".png") != -1) {
                copyFileIfNeed(context, str, className);
            }
        }
        File file = new File(folderpath);
        File[] subFile = file.listFiles();
        if (subFile == null || subFile.length == 0) {
            return iconFiles;
        }
        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为png结尾
                if (filename.trim().toLowerCase().endsWith(".png") && filename.indexOf("mode_") == -1) {
                    String name = subFile[i].getName();
                    iconFiles.put(getFileNameNoEx(name), BitmapFactory.decodeFile(filename));
                }
            }
        }
        return iconFiles;
    }

    public static Map<String, Bitmap> getStickerIconFilesFromSd(Context context, String className) {
        TreeMap<String, Bitmap> iconFiles = new TreeMap<String, Bitmap>();
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;
            File folder = new File(folderpath);
            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        File file = new File(folderpath);
        File[] subFile = file.listFiles();
        if (subFile == null || subFile.length == 0) {
            return iconFiles;
        }
        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为png结尾
                if (filename.trim().toLowerCase().endsWith(".png") && filename.indexOf("mode_") == -1) {
                    String name = subFile[i].getName();
                    iconFiles.put(getFileNameNoEx(name), BitmapFactory.decodeFile(filename));
                }
            }
        }
        return iconFiles;
    }

    public static List<String> getStickerNames(Context context, String className) {
        ArrayList<String> modelNames = new ArrayList<String>();
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;
            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        File file = new File(folderpath);
        File[] subFile = file.listFiles();
        if (subFile == null || subFile.length == 0) {
            return modelNames;
        }
        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".zip") || filename.trim().toLowerCase().endsWith(".model")) {
                    String name = subFile[i].getName();
                    modelNames.add(getFileNameNoEx(name));
                }
            }
        }
        return modelNames;
    }

//    public static ArrayList<FilterItem> getFilterFiles(Context context, String index) {
//        ArrayList<FilterItem> filterFiles = new ArrayList<FilterItem>();
//        Bitmap iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.mode_original);
//        if (Constants.FILTER_PORTRAIT.equals(index)) {
//            iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.filter_portrait_nature);
//        } else if (Constants.FILTER_SCENERY.equals(index)) {
//            iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.filter_scenery_nature);
//        } else if (Constants.FILTER_STILL_LIFE.equals(index)) {
//            iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.filter_still_life_nature);
//        } else if (Constants.FILTER_FOOD.equals(index)) {
//            iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.filter_food_nature);
//        }
//        filterFiles.add(new FilterItem(Constants.ORIGINAL, iconNature, null));
//        List<String> filterModels = copyFilterModelFiles(context, index);
//        Map<String, Bitmap> filterIcons = copyFilterIconFiles(context, index);
//        List<String> filterNames = getFilterNames(context, index);
//        if (filterModels.size() == 0) {
//            return filterFiles;
//        }
//        for (int i = 0; i < filterModels.size(); i++) {
//            if (filterIcons.get(filterNames.get(i)) != null) {
//                filterFiles.add(new FilterItem(filterNames.get(i), filterIcons.get(filterNames.get(i)), filterModels.get(i)));
//            } else {
//                filterFiles.add(new FilterItem(filterNames.get(i), iconNature, filterModels.get(i)));
//            }
//        }
//        return filterFiles;
//    }

    public static List<String> copyFilterModelFiles(Context context, String index) {
        String[] files = null;
        ArrayList<String> modelFiles = new ArrayList<String>();
        try {
            files = context.getAssets().list(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + index;

            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        for (int i = 0; i < files.length; i++) {
            String str = files[i];
            if (str.indexOf(".model") != -1) {
                copyFileIfNeed(context, str, index);
            }
        }
        File file = new File(folderpath);
        File[] subFile = file.listFiles();
        if (subFile == null || subFile.length == 0) {
            return modelFiles;
        }
        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".model") && filename.indexOf("filter") != -1) {
                    modelFiles.add(filename);
                }
            }
        }
        return modelFiles;
    }

    public static Map<String, Bitmap> copyFilterIconFiles(Context context, String index) {
        String[] files = null;
        TreeMap<String, Bitmap> iconFiles = new TreeMap<String, Bitmap>();
        try {
            files = context.getAssets().list(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + index;
            File folder = new File(folderpath);
            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        for (int i = 0; i < files.length; i++) {
            String str = files[i];
            if (str.indexOf(".png") != -1) {
                copyFileIfNeed(context, str, index);
            }
        }
        File file = new File(folderpath);
        File[] subFile = file.listFiles();
        if (subFile == null || subFile.length == 0) {
            return iconFiles;
        }
        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为png结尾
                if (filename.trim().toLowerCase().endsWith(".png") && filename.indexOf("filter") != -1) {
                    String name = subFile[i].getName().substring(13);
                    iconFiles.put(getFileNameNoEx(name), BitmapFactory.decodeFile(filename));
                }
            }
        }
        return iconFiles;
    }

    public static List<String> getFilterNames(Context context, String index) {
        ArrayList<String> modelNames = new ArrayList<String>();
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + index;
            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        File file = new File(folderpath);
        File[] subFile = file.listFiles();
        if (subFile == null || subFile.length == 0) {
            return modelNames;
        }
        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".model") && filename.indexOf("filter") != -1) {
                    String name = subFile[i].getName().substring(13);
                    modelNames.add(getFileNameNoEx(name));
                }
            }
        }
        return modelNames;
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

//    public static ArrayList<MakeupItem> getMakeupFiles(Context context, String index) {
//        ArrayList<MakeupItem> makeupFiles = new ArrayList<MakeupItem>();
//        Bitmap iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.makeup_null);
//        if (Constants.MAKEUP_LIP.equals(index)) {
//            iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.makeup_null);
//        } else if (Constants.MAKEUP_HIGHLIGHT.equals(index)) {
//            iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.makeup_null);
//        } else if (Constants.MAKEUP_BLUSH.equals(index)) {
//            iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.makeup_null);
//        } else if (Constants.MAKEUP_BROW.equals(index)) {
//            iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.makeup_null);
//        } else if (Constants.GROUP_EYE.equals(index)) {
//            iconNature = BitmapFactory.decodeResource(context.getResources(), R.drawable.makeup_null);
//        }
//        makeupFiles.add(new MakeupItem(Constants.ORIGINAL, iconNature, null));
//        List<String> makeupZips = getStickerZipFilesFromSd(context, index);
//        Map<String, Bitmap> makeupIcons = getStickerIconFilesFromSd(context, index);
//        List<String> makeupNames = getStickerNames(context, index);
//        if (makeupZips.size() == 0) {
//            return makeupFiles;
//        }
//        Collections.sort(makeupZips);
//        Collections.sort(makeupNames);
//        for (int i = 0; i < makeupZips.size(); i++) {
//            if (makeupIcons.get(makeupNames.get(i)) != null) {
//                makeupFiles.add(new MakeupItem(makeupNames.get(i), makeupIcons.get(makeupNames.get(i)), makeupZips.get(i)));
//            } else {
//                makeupFiles.add(new MakeupItem(makeupNames.get(i), iconNature, makeupZips.get(i)));
//            }
//        }
//        return makeupFiles;
//    }
}
