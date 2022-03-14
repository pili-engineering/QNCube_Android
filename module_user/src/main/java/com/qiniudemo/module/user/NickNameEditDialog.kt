package com.qiniudemo.module.user

import android.view.Gravity
import androidx.lifecycle.lifecycleScope
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.BaseDialogFragment
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniudemo.baseapp.service.UserService
import kotlinx.android.synthetic.main.user_nick_edit_dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NickNameEditDialog : BaseDialogFragment() {


    init {
        applyGravityStyle(Gravity.CENTER)
    }

    override fun initViewData() {
       tvOk.setOnClickListener {
           val etNameStr = etName.text.toString()
           if(etNameStr.isEmpty()){
               return@setOnClickListener
           }
           lifecycleScope.launch(Dispatchers.Main) {
               showLoading(true)
               try {
                   RetrofitManager.create(UserService::class.java).editUserInfo(UserInfoManager.getUserId(),etNameStr)
               }catch (e:Exception){
                   e.printStackTrace()
               }finally {
                   showLoading(false)
               }
               dismiss()
           }
       }
        tvCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun getViewLayoutId(): Int {
        return R.layout.user_nick_edit_dialog
    }
}