package com.qiniu.droid.audio2text

import android.annotation.SuppressLint
import android.content.Context

/**
 * 配置类 需要初始化接入方的参数
 */
@SuppressLint("StaticFieldLeak")
object QNRtcAiConfig {
    var signCallback : QNRtcAISdkManager.SignCallback?=null    //  请求url签名回调

}
