package com.qiniudemo.baseapp.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.alibaba.android.arouter.launcher.ARouter
import com.hapi.happy_dialog.FinalDialogFragment
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.widget.CommonTipDialog


object SchemaParser {

    fun parseRouter(
        context: Context,
        fm: FragmentManager,
        url: String,
        httpEnable: Boolean = true
    ): Boolean {
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
                isSupport = true
                CommonTipDialog.TipBuild()
                    .setContent("是否使用低代码直播ktv？")
                    .setListener(object : FinalDialogFragment.BaseDialogListener() {
                        override fun onDialogNegativeClick(dialog: DialogFragment, any: Any) {
                            super.onDialogNegativeClick(dialog, any)
                            when (path) {
                                "/index" -> ARouter.getInstance().build(RouterConstant.KTV.KTVList)
                                    .withString("solutionType", param)
                                    .navigation(context)
                            }
                        }

                        override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                            super.onDialogPositiveClick(dialog, any)
                            ARouter.getInstance()
                                .build(RouterConstant.LowCodePKLive.LiveRoomList)
                                .withInt("layoutType", 3)
                                .navigation(context)
                        }
                    })
                    .build()
                    .show(fm, "CommonTipDialog")

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
                when (path) {
                    "/index" -> ARouter.getInstance()
                        .build(RouterConstant.LowCodePKLive.LiveRoomList)
                        .withInt("layoutType", 2)
                        .navigation(context)
                }
                isSupport = true
            }
            "shopping" -> {
                when (path) {
                    "/index" -> ARouter.getInstance()
                        .build(RouterConstant.LowCodePKLive.LiveRoomList)
                        .withInt("layoutType", 1)
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