package com.qiniudemo.baseapp.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.qiniu.router.RouterConstant


object SchemaParser {

    fun parseRouter(context: Context, url: String, httpEnable: Boolean = true): Boolean {
        val router = Uri.parse(url)
        val scheme = router.scheme
        val host = router.host
        val path = router.path
        if (httpEnable) {
            if (scheme?.startsWith("http") == true) {
                // WebActivity.start(url, context)
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
                return true
            }
        }
        var isSupport = false
        when (host) {
            "interview" -> {
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.Interview.InterviewList)
                        .navigation(context)
                    "/joinInterview" -> {
                        val param = router.getQueryParameter("interviewId")
                        ARouter.getInstance().build(RouterConstant.Interview.InterviewRoom)
                            .withString("interviewId", param).navigation(context)
                    }
                    "/updateInterview" -> {
                        val param = router.getQueryParameter("interviewId")
                        ARouter.getInstance().build(RouterConstant.Interview.InterviewCreate)
                            .withString("interviewId", param).navigation(context)
                    }
                }
                isSupport = true
            }
            "repair" -> {
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.Overhaul.OverhaulList)
                        .navigation(context)
                }
                isSupport = true
            }

            "ktv" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.KTV.KTVList)
                        .withString("solutionType", param)
                        .navigation(context)
                }
                isSupport = true
            }
            "show" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.Amusement.AmusementList)
                        .withString("solutionType", param)
                        .navigation(context)
                }
                isSupport = true
            }
            "movie" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.VideoRoom.VideoListHome)
                        .withString("solutionType", param)
                        .navigation(context)
                }
                isSupport = true
            }
            "voiceChatRoom" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance()
                        .build(RouterConstant.VoiceChatRoom.voiceChatRoomList)
                        .withString("solutionType", param)
                        .navigation(context)
                }
                isSupport = true
            }
            "liveKit" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance()
                        .build(RouterConstant.LowCodePKLive.LiveRoomList)
                        .withString("solutionType", param)
                        .withBoolean("needShopping",false)
                        .navigation(context)
                }
                isSupport = true
            }
            "shopping" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance()
                        .build(RouterConstant.LowCodePKLive.LiveRoomList)
                        .withString("solutionType", param)
                        .withBoolean("needShopping",true)
                        .navigation(context)
                }
                isSupport = true
            }

            else -> {
                isSupport = false
            }
        }

        return isSupport
    }

}