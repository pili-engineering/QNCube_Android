package com.qiniudemo.login

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.hipi.vm.LifecycleUiCall
import com.hipi.vm.lazyVm
import com.qiniu.login.databinding.ActivityLoginBinding
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.BaseStartActivity.Companion.loginFinishPostcard
import com.qiniudemo.baseapp.vm.LoginVm
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.manager.swith.EnvSwitchDialog
import com.qiniudemo.baseapp.manager.swith.SwitchEnvHelper
import com.qiniudemo.baseapp.web.WebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = RouterConstant.Login.LOGIN)
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val loginVm by lazyVm<LoginVm>()
    private fun timeJob() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                binding.tvSmsTime.isClickable = false
                repeat(60) {
                    binding.tvSmsTime.text = (60 - it).toString()
                    delay(1000)
                }
                binding.tvSmsTime.text = "获取验证码"
                binding.tvSmsTime.isClickable = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun init() {

        if (SwitchEnvHelper.get().isSwitchEnable) {
            binding.clRoot.setOnLongClickListener {
                EnvSwitchDialog(this).show()
                true
            }
        }

        binding.tvSmsTime.setOnClickListener {
            val phone = binding.etLoginPhone.text.toString() ?: ""
            loginVm.getVerificationCode(phone, LifecycleUiCall(lifecycle = this) {
                binding.etLoginVerificationCode.requestFocus()
                timeJob()
            })
        }

        binding.etLoginPhone.setOnClickListener {
            val phone = binding.etLoginPhone.text.toString() ?: ""
            val code = binding.etLoginVerificationCode.text.toString() ?: ""
            if (phone.isEmpty()) {
                "请输入手机号".asToast()
                return@setOnClickListener
            }
            if (code.isEmpty()) {
                "请输入验证码".asToast()
                return@setOnClickListener
            }

            if (!binding.cbAgreement.isSelected) {
                "请同意 七牛云服务用户协议 和 隐私权政策".asToast()
                return@setOnClickListener
            }

            loginVm.login(phone, code, LifecycleUiCall(this) {
                if (it) {
                    loginFinishPostcard?.navigation(this)
                    finish()
                }
            })
        }

        binding.cbAgreement.setOnClickListener {
            binding.cbAgreement.isSelected = !binding.cbAgreement.isSelected
        }
        val tips = "我已阅读并同意 七牛云服务用户协议 和 隐私权政策"
        val spannableString = SpannableString(tips)
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                WebViewActivity.start("https://www.qiniu.com/privacy-right", this@LoginActivity)
            }
        }, tips.length - 5, tips.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                WebViewActivity.start("https://www.qiniu.com/user-agreement", this@LoginActivity)
            }
        }, tips.length - 18, tips.length - 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            tips.length - 5,
            tips.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            tips.length - 18,
            tips.length - 7,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#007AFF")),
            tips.length - 5,
            tips.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#007AFF")),
            tips.length - 18,
            tips.length - 7,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.cbAgreement.setMovementMethod(LinkMovementMethod.getInstance());//设置可点击状态
        binding.cbAgreement.text = spannableString
    }

}