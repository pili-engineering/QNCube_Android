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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * 粘贴板工具类
 * Created by xx on 2016/12/22.
 */

public class ClipboardUtil {

    /**
     * 复制文本
     *
     * @param txt
     */
    public static void saveTextToClipboard(Context context, String txt) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", txt);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * 复制图片
     *
     * @param path
     */
    public static void savePicToClipboard(Context context, String path) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newUri(context.getContentResolver(), "image", Uri.parse(path));
        clipboard.setPrimaryClip(clip);
    }

    /**
     * 复制图片
     *
     * @param intent
     */
    public static void saveIntentToClipboard(Context context, Intent intent) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newIntent("intent", intent);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * 获取粘贴板文本内容
     *
     * @param context
     */
    public String getClipboardText(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        int count = clip.getItemCount();
        return clip.getItemAt(count - 1).coerceToText(context).toString();
    }

    /**
     * 获取粘贴板文本Uri内容
     *
     * @param context
     */
    public String getClipboardUri(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        int count = clip.getItemCount();
        return clip.getItemAt(count - 1).coerceToText(context).toString();
    }

    /**
     * 获取粘贴板文本Intent内容
     *
     * @param context
     */
    public String getClipboardIntent(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        int count = clip.getItemCount();
        return clip.getItemAt(count - 1).coerceToText(context).toString();
    }
}
