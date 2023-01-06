package com.qiniudemo.module.user

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.hapi.baseframe.dialog.FinalDialogFragment


import com.bumptech.glide.Glide
import com.hapi.mediapicker.ImagePickCallback
import com.hapi.mediapicker.PicPickHelper
import com.hapi.mediapicker.Size
import com.hipi.vm.bgDefault
import com.hipi.vm.lifecycleBg
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.BaseFragment
import com.qiniu.bzcomp.user.UserInfo
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniudemo.baseapp.service.LoginService
import com.qiniudemo.baseapp.service.UserService
import com.qiniudemo.baseapp.web.WebViewActivity
import com.qiniudemo.module.user.databinding.UserFragmentMineBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MineFragment : BaseFragment<UserFragmentMineBinding>() {
    private val mPicPickHelper by lazy { PicPickHelper(requireActivity() as AppCompatActivity) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        UserInfoManager.getUserInfo()?.let {
            refreshUinfo(it)
        }

        binding.flPrivacy.setOnClickListener {
            WebViewActivity.start("https://www.qiniu.com/privacy-right", requireContext())
        }
        binding.flLiability.setOnClickListener {
            WebViewActivity.start("https://www.qiniu.com/user-agreement", requireContext())
        }
        binding.flUpLoadLog.setOnClickListener {
        }
        binding.ivAvatar.setOnClickListener {
            mPicPickHelper.show(Size(1, 1), object : ImagePickCallback {
                override fun onSuccess(result: String?, url: Uri?) {
                    Log.d("mPicPickHelper", " onSuccess $result ${url?.toString()}")
                    if (result?.isEmpty() == true) {
                        return
                    }
                    val file = File(result!!)
                    lifecycleBg {
                        showLoading(true)
                        doWork {
                            val requestFile =
                                file.asRequestBody(("multipart/form-data").toMediaType())
                            val body =
                                MultipartBody.Part.createFormData("file", file.name, requestFile)
                            val remoteFile =
                                RetrofitManager.create(UserService::class.java).upload(body)
                            RetrofitManager.create(UserService::class.java)
                                .editUserAvatar(UserInfoManager.getUserId(), remoteFile.fileUrl)
                            refreshUinfo()
                        }
                        catchError {
                            it.printStackTrace()
                        }
                        onFinally {
                            showLoading(false)
                        }
                    }
                }
            })
        }
        binding.flLoginOut.setOnClickListener {
            lifecycleBg {
                showLoading(true)
                doWork {
                    RetrofitManager.create(LoginService::class.java)
                        .signOut()
                }
                onFinally {
                    showLoading(false)
                    UserInfoManager.onLogout()
                }
            }
        }

        binding.tvProfile.setOnClickListener {
            NickNameEditDialog().setDefaultListener(object :
                FinalDialogFragment.BaseDialogListener() {
                override fun onDismiss(dialog: DialogFragment) {
                    super.onDismiss(dialog)
                    refreshUinfo()
                }
            }).show(childFragmentManager, "NickNameEditDialog")
        }
        binding.tvName.setOnClickListener {
            binding.tvProfile.performClick()
        }
        binding.tvSdkVersion.text = BuildConfig.rtcSdkVersion
        binding.tvVersionTime.text = BuildConfig.releaseTime
        val verName = requireActivity().packageManager.getPackageInfo(
            requireActivity().packageName,
            0
        ).versionName
        binding.tvAppVersion.text = verName
    }

    override fun onResume() {
        super.onResume()
        refreshUinfo()
    }

    private fun refreshUinfo() {
        bgDefault {
            val info = RetrofitManager.create(UserService::class.java)
                .getUserInfo(UserInfoManager.getUserId())
            UserInfoManager.updateUserInfo(info)
            refreshUinfo(info)
        }
    }

    private fun refreshUinfo(userInfo: UserInfo) {
        Glide.with(requireActivity())
            .load(userInfo.avatar)
            .into(binding.ivAvatar)
        binding.tvName.text = userInfo.nickname
        binding.tvProfile.text = userInfo.profile
    }

}