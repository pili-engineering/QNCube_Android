package com.qiniudemo.baseapp.manager

import android.content.Context
import android.net.Uri
import com.alibaba.android.arouter.launcher.ARouter
import com.qiniu.router.RouterConstant
import com.qiniudemo.webview.WebActivity

object SchemaParser {

    fun parseRouter(context: Context, url: String) {

        val router = Uri.parse(url)
        val scheme = router.scheme
        val host = router.host
        val path = router.path
        if (scheme?.startsWith("http") == true) {
            WebActivity.start(url, context)
            return
        }
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
            }
            "repair" -> {
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.Overhaul.OverhaulList)
                        .navigation(context)
                }
            }

            "ktv" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.KTV.KTVList)
                        .withString("solutionType", param)
                        .navigation(context)
                }
            }
            "show" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.Amusement.AmusementList)
                        .withString("solutionType", param)
                        .navigation(context)
                }
            }
            "movie" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.VideoRoom.VideoListHome)
                        .withString("solutionType", param)
                        .navigation(context)
                }
            }
            "voiceChatRoom" -> {
                val param = router.getQueryParameter("type")
                when (path) {
                    "/index" -> ARouter.getInstance().build(RouterConstant.VoiceChatRoom.voiceChatRoomList)
                        .withString("solutionType", param)
                        .navigation(context)
                }
            }
        }

    }

}