package com.niucube.overhaul

import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.niucube.overhaul.databinding.ActivityCreateRoomBinding
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.ext.asToast

class CreateRoomActivity : BaseActivity<ActivityCreateRoomBinding>() {

    override fun init() {

        binding.btnConfirm.isEnabled = false
        binding.  etRoomTittle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                binding.   btnConfirm.isEnabled = !TextUtils.isEmpty(p0?.toString())
            }
        })
        binding. btnConfirm.setOnClickListener {
            val etTittle =  binding. etRoomTittle.text.toString()
            val roloId =  binding. rgRole.checkedRadioButtonId

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