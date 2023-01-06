package com.hapi.baseframe.activity

import android.view.View
import androidx.viewbinding.ViewBinding
import com.hapi.baseframe.ext.ViewBindingExt
import java.lang.reflect.ParameterizedType

abstract class BaseBindingActivity<T : ViewBinding> : BaseFrameActivity() {

    lateinit var binding: T
    override fun createContainerView(): View? {
        var sup = javaClass.genericSuperclass
        if (sup !is ParameterizedType) {
            sup = javaClass.superclass.genericSuperclass
        }
        binding = ViewBindingExt.create(sup as ParameterizedType, null, this, false)
        return binding.root
    }

    final override fun getLayoutId(): Int {
        return -1
    }
}