package com.hapi.baseframe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.hapi.baseframe.ext.ViewBindingExt
import java.lang.reflect.ParameterizedType

abstract class BaseBindingFragment<T : ViewBinding> : BaseFrameFragment() {

    lateinit var binding: T
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sup = javaClass.genericSuperclass
        binding = ViewBindingExt.create(sup as ParameterizedType, null, requireContext(), false)
        return binding.root
    }

    final override fun getLayoutId(): Int {
        return -1
    }
}