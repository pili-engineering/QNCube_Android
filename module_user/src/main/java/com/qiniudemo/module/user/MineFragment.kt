package com.qiniudemo.module.user

import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.hapi.happy_dialog.FinalDialogFragment


import com.bumptech.glide.Glide
import com.hapi.mediapicker.ImagePickCallback
import com.hapi.mediapicker.PicPickHelper
import com.hapi.mediapicker.Size
import com.hipi.vm.backGround
import com.hipi.vm.bgDefault
import com.hipi.vm.lifecycleBg
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.BaseFragment
import com.qiniu.bzcomp.user.UserInfo
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.manager.swith.EnvType
import com.qiniudemo.baseapp.manager.swith.SwitchEnvHelper
import com.qiniudemo.baseapp.service.LoginService
import com.qiniudemo.baseapp.service.UserService
import com.qiniudemo.webview.WebActivity
import kotlinx.android.synthetic.main.user_fragment_mine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MineFragment : BaseFragment() {
    private val mPicPickHelper by lazy { PicPickHelper(requireActivity() as AppCompatActivity) }
    override fun initViewData() {
        UserInfoManager.getUserInfo()?.let {
            refreshUinfo(it)
        }

        flPrivacy.setOnClickListener {
            WebActivity.start("https://www.qiniu.com/privacy-right", requireContext())
        }
        flLiability.setOnClickListener {
            WebActivity.start("https://www.qiniu.com/user-agreement", requireContext())
        }
        flUpLoadLog.setOnClickListener {
        }
        ivAvatar.setOnClickListener {
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
        flLoginOut.setOnClickListener {
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

        tvProfile.setOnClickListener {
            NickNameEditDialog().setDefaultListener(object :
                FinalDialogFragment.BaseDialogListener() {
                override fun onDismiss(dialog: DialogFragment) {
                    super.onDismiss(dialog)
                    refreshUinfo()
                }
            }).show(childFragmentManager, "NickNameEditDialog")
        }
        tvName.setOnClickListener {
            tvProfile.performClick()
        }
        tvSdkVersion.text = BuildConfig.rtcSdkVersion
        tvVersionTime.text = BuildConfig.releaseTime
        val verName = requireActivity().packageManager.getPackageInfo(
            requireActivity().packageName,
            0
        ).versionName
        tvAppVersion.text = verName
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
            .into(ivAvatar)
        tvName.text = userInfo.nickname
        tvProfile.text = userInfo.profile
    }

    override fun getLayoutId(): Int {
        return R.layout.user_fragment_mine
    }
}