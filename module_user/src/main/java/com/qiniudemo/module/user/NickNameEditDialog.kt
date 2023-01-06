package com.qiniudemo.module.user

import android.view.Gravity
import androidx.lifecycle.lifecycleScope
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.BaseDialogFragment
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniudemo.baseapp.service.UserService
import com.qiniudemo.module.user.databinding.UserNickEditDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NickNameEditDialog : BaseDialogFragment<UserNickEditDialogBinding>() {

    init {
        applyGravityStyle(Gravity.CENTER)
    }

    override fun initViewData() {
        binding.tvOk.setOnClickListener {
            val etNameStr = binding.etName.text.toString()
            if (etNameStr.isEmpty()) {
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.Main) {
                showLoading(true)
                try {
                    RetrofitManager.create(UserService::class.java)
                        .editUserInfo(UserInfoManager.getUserId(), etNameStr)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    showLoading(false)
                }
                dismiss()
            }
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

}