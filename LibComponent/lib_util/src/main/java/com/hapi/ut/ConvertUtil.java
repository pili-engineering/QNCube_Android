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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConvertUtil {
    public static byte[] is2Bytes(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }

    public static String isToString(InputStream is) {
        if (is != null) {
            byte[] buffer = new byte[1024 * 4];
            StringBuffer sb = new StringBuffer();
            int len = 0;
            try {
                while ((len = is.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, len, "utf-8"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                        is = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    public static byte[] bmp2Bytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bmp(byte[] bs) {
        if (bs == null) {
            return null;
        }
        Bitmap bmpout = BitmapFactory.decodeByteArray(bs, 0, bs.length);
        return bmpout;
    }

    /**
     * 16进制字符串转化为字节数组
     * 00 ff -> [0,-1]
     *
     * @param res
     * @return
     */
    public static byte[] hexStringToByteArray(String res) {
        if (res.length() % 2 == 0) {
            byte[] result = new byte[res.length() / 2];
            for (int i = 0; i < res.length() / 2; i++) {
                String val = res.substring(i * 2, i * 2 + 2);
                result[i] = (byte) Integer.parseInt(val, 16);
            }
            return result;
        }
        return null;
    }

    /**
     * 字节数组转化为16进制字符串
     * [0,-1] ->00 ff
     *
     * @param bytes
     * @return
     */
    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}