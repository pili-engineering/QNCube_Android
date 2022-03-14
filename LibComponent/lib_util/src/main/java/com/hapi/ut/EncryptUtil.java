package com.hapi.ut;

import android.text.TextUtils;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author xx
 * @date 2018/6/8
 */

public class EncryptUtil {

    /**
     * 描述：MD5加密.
     *
     * @param str 要加密的字符串
     * @return String 加密的字符串
     */
    public final static String MD5(String str) {
        char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
                'e', 'f'};
        try {
            byte[] strTemp = str.getBytes();
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte tmp[] = mdTemp.digest(); // MD5 的计算结果是一个 128 位的长整数，
            // 用字节表示就是 16 个字节
            char strs[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
            // 所以表示成 16 进制需要 32 个字符
            int k = 0; // 表示转换结果中对应的字符位置
            for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
                // 转换成 16 进制字符的转换
                byte byte0 = tmp[i]; // 取第 i 个字节
                strs[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
                // >>> 为逻辑右移，将符号位一起右移
                strs[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
            }
            return new String(strs).toUpperCase(); // 换后的结果转换为字符串
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取文件的MD5
     *
     * @param file
     * @return
     */
    public static String getFileMd5(File file) {
        if (file == null || !file.exists()) {
            return "";
        }
        InputStream is = null;
        MessageDigest md = null;
        try {
            is = new FileInputStream(file);
            md = MessageDigest.getInstance("MD5");
            byte buffer[] = new byte[1024];
            int len;
            while ((len = is.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, len);
            }
            byte[] digest = md.digest();

            String fileMD5 = new BigInteger(1, digest).toString(16);
            while (fileMD5.length() < 32) {
                fileMD5 = "0" + fileMD5;
            }
            return fileMD5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
        return "";
    }

    /**
     * 检查文件的MD5
     *
     * @param file
     * @param serverMD5
     * @return
     */
    public static boolean checkFileMd5(File file, String serverMD5) {
        String fileMD5 = getFileMd5(file);
        if (TextUtils.isEmpty(fileMD5) || TextUtils.isEmpty(serverMD5)) {
            return false;
        }
        return serverMD5.toUpperCase().equals(fileMD5.toUpperCase());
    }

    /**
     * 加密传入的数据是byte类型的，并非使用decode方法将原始数据转二进制，String类型的数据 使用 str.getBytes()即可
     */
    public static String encode64(String str) {
        return encode64(str.getBytes());
    }

    public static String encode64(byte[] bytes) {
        // 在这里使用的是encode方式，返回的是byte类型加密数据，可使用new String转为String类型
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * 对base64加密后的数据进行解密
     */
    public static String decode64(String strBase64) {
        return new String(Base64.decode(strBase64.getBytes(), Base64.DEFAULT));
    }
}
