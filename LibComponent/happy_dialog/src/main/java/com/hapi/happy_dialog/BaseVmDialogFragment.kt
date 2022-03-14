package com.hapi.happy_dialog

import com.hipi.vm.LoadingObserverView


abstract class BaseVmDialogFragment: FinalDialogFragment(), LoadingObserverView {

    override fun init() {
        observeLiveData()
        initViewData()
    }

    /**
     * 订阅vm
     * 如果使用自己的vm 也可以单独订阅一个fragment
     */
    abstract fun observeLiveData()

    abstract fun initViewData()
}