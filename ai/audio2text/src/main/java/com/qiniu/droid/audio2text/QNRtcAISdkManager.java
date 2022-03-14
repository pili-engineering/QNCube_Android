package com.qiniu.droid.audio2text;

import android.content.Context;

/**
 * sdk 初始化类
 */
public class QNRtcAISdkManager {

    /**
     * 初始化Aisdk
     * @param signCallback  请求url签名回调
     */
    public static void init(
            SignCallback signCallback   //  请求url签名回调
     ) {
        QNRtcAiConfig.INSTANCE.setSignCallback(signCallback);

    }

   public  interface  SignCallback{
       /**
        *  url签名回调 工作线程
        * @param url 请求URL
        * @return 签名后的token
        */
        public String signUrlToToken(String url);
   }
}
