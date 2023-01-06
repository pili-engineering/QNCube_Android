package com.hapi.baseframe.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.hapi.baseframe.ext.ViewBindingExt
import com.hipi.vm.LoadingObserverView
import java.lang.reflect.ParameterizedType


abstract class BaseVmBindingDialogFragment<T : ViewBinding> : FinalDialogFragment(),
    LoadingObserverView {

    lateinit var binding: T
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var sup = javaClass.genericSuperclass
        if(sup !is ParameterizedType){
            sup = javaClass.superclass.genericSuperclass
        }
        binding = ViewBindingExt.create(sup as ParameterizedType, null, requireContext(), false)
        return binding.root
    }

    final override fun getViewLayoutId(): Int {
        return -1
    }

    override fun init() {
        observeLiveData()
        initViewData()
    }

    /**
     * 订阅vm
     * 如果使用自己的vm 也可以单独订阅一个fragment
     */
    open fun observeLiveData(){}

    abstract fun initViewData()
}