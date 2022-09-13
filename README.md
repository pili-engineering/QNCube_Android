# niucube_android

## 1 概述
牛魔方是七牛云推出的一款基于[七牛rtc](https://github.com/pili-engineering/QNRTC-Android/blob/master/README.md)的方案研发demo集合app,包含了面试场景、工业检修、在线教育、在线ktv、互动娱乐、一起看电影、在线考试，语聊房、pk直播解决方案demo。


## 2 模块说明
```

include ':app'                                // app主工程

//appsub
include ':appsub:app_watchvideo'            //独立运行一起看电影
include ':appsub:app_ktv'                   //独立运行ktv
include ':appsub:app_amusement'             //独立运行互动娱乐
include ':appsub:app_overhaul'              //独立运行检修场景
include ':appsub:app_audioroom'             //独立运行语聊房间
include ':appsub:app_lowcodelive'

//场景模块
include ':module_interview'                   //面试场景
include ':module_overhaul'                    //检修
include ':module_ktv'                         //ktv
include ':module_amusement'                   //互动娱乐
include ':module_videowatch'                  //一起看电影
include ':module_audioroom'                   //语聊房间
include ':module_lowcodeliving'               //直播

// highlevlsdk
include ':1highlevlsdk:comp_player'              //播放器
include ':1highlevlsdk:comp_roommanager'         //房间生命周期管理
include ':1highlevlsdk:comp_rtm'                 //im 信令
include ':1highlevlsdk:qrtcroom'                 //rtc

//ui组件
include ':BzUiComponent:bzuicomp_pagerroom'      //分页房间
include ':BzUiComponent:bzuicomp_bottomInput'    //房间底部输入框
include ':BzUiComponent:bzuicomp_pubchat'        //房间公屏聊天
include ':BzUiComponent:bzuiEmoji'               //
include ':BzUiComponent:bzuicomp_whitebord'      // 白板UI
include ':BzUiComponent:bzuicomp_gift'           //礼物轨道
include ':BzUiComponent:compui_trackview'        //轨道管理
include ':BzUiComponent:bzuicomp_danmu'          //弹幕
include ':BzUiComponent:compui_lrcview'          //歌词组件
include ':BzUiComponent:bzuicomp_chatdialog'     //聊天弹窗
include ':BzUiComponent:sdk_qndroidwhiteboard'
include ':BzUiComponent:rtclogview'
```

## 快速开始
### 运行app工程
克隆工程后，目标application可以选择 app模块(牛魔方主app) 和 appsub目录的子工程。服务端url链接的是牛魔方服务器无需修改任何配置。
### 调试服务端
如果需要调试服务端，部署牛魔方开源服务端项目后修改 baseApp/build.gradle 第八行 def base_url =https://niucube-api.qiniu.com/ 改为自己部署的服务费则可以实现部署服务端连调。


## 反馈及意见
当你遇到任何问题时，可以通过在 GitHub 的 repo 提交 issues 来反馈问题，请尽可能的描述清楚遇到的问题，如果有错误信息也一同附带，并且在 Labels 中指明类型为 bug 或者其他。

## 体验demo
用手机浏览器输入这个网址: [ http://fir.qnsdk.com/s6py]( http://fir.qnsdk.com/s6py)

## 预览

 **app**

![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/app.jpeg)
**面试**


| ![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/mslb.jpeg) | ![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/nianshi1de5.jpeg) |
|:--|---|


**看电影**



| ![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/dianyliaot.jpeg) | ![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/dy.jpeg) |
|---|---|
| ![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/dyhep.jpeg) |



**检修**
![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/jianxiu.jpeg)

**语聊**


| ![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/yul.jpeg) | ![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/yulgift.jpeg) | ![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/yuliao.jpeg) |
|---|---|---|





**ktv**
![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/ktv.jpeg)

**互动娱乐**
![](http://qrnlrydxa.hn-bkt.clouddn.com/cubupngandroid/hy.jpeg)
