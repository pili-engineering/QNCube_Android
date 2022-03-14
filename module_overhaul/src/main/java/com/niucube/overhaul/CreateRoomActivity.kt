package com.niucube.overhaul

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.core.widget.addTextChangedListener
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.ext.asToast
import kotlinx.android.synthetic.main.activity_create_room.*

class CreateRoomActivity : BaseActivity() {

    override fun initViewData() {

        btnConfirm.isEnabled = false
        etRoomTittle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                btnConfirm.isEnabled = !TextUtils.isEmpty(p0?.toString())
            }
        })
        btnConfirm.setOnClickListener {
            val etTittle = etRoomTittle.text.toString()
            val roloId = rgRole.checkedRadioButtonId

            if (etTittle.isEmpty()) {
                "请输入房间标题".asToast()
                return@setOnClickListener
            }
            val role = if (roloId == R.id.rbStaff) {
                OverhaulRole.STAFF.role
            } else {
                OverhaulRole.PROFESSOR.role
            }
//            mDefaultListener?.onDialogPositiveClick(this,Params(etTittle,role))
//            dismiss()

            setResult(RESULT_OK, Intent().apply {
                putExtra("etTittle",etTittle)
                putExtra("roleId",role)
            })
            finish()
        }
    }

    override fun isTranslucentBar(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
       return R.layout.activity_create_room
    }

    override fun getInitToolBarTitle(): String {
        return "创建房间"
    }

    override fun isTitleCenter(): Boolean {
        return true
    }

    override fun isToolBarEnable(): Boolean {
        return true
    }
}