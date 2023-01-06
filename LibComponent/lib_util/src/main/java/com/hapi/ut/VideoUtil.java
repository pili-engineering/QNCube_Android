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
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xx on 2016/10/8.
 */
public class VideoUtil {

    /**
     * 生成视频文件的缩略图
     * (获取第6us)
     *
     * @param path
     * @return
     */
    public static Bitmap createVideoThumbnail(String path) {
        File file = FileUtil.openFile(path, false);
        if (file == null) {
            return null;
        }
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            bitmap = retriever.getFrameAtTime(6); //取得指定时间（第6us）的Bitmap，即可以实现抓图（缩略图）功能
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                assert retriever != null;
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 根据视频文件创建指定宽高的缩略图
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createVideoThumbnail(String path, int width, int height) {
        Bitmap bitmap = createVideoThumbnail(path);
        if (bitmap == null) {
            return null;
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return bitmap;
    }

    /**
     * 获取视频文件的时长
     *
     * @param videoPath
     * @return ms
     */
    public static long getVideoLength(String videoPath) {
        long video_length = 0;
        try {
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(videoPath);
            video_length = Long.parseLong(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return video_length;
    }

    /**
     * 获取视频文件内存大小
     *
     * @param videoPath
     * @return 字节数
     */
    public static long getVideoSize(String videoPath) {
        return FileUtil.getFileSize(videoPath);
    }

    /**
     * 解析本地视频文件，获取缩略图，时长，大小
     *
     * @param context
     * @param videoPath
     * @return
     */
    public static Map<String, Object> parseVideo(Context context, String videoPath) {
        File file = FileUtil.openFile(videoPath, false);
        if (file == null) {
            return null;
        }
        Map<String, Object> map = null;
        MediaMetadataRetriever media = null;
        try {
            media = new MediaMetadataRetriever();
            media.setDataSource(context, Uri.fromFile(file));
            Bitmap bitmap = media.getFrameAtTime(6); //取得指定时间（第6us）的Bitmap，即可以实现抓图（缩略图）功能
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
}
