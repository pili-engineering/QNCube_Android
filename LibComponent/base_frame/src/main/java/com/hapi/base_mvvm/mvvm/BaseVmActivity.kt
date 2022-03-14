package com.hapi.base_mvvm.mvvm


import com.hapi.base_mvvm.activity.BaseFrameActivity
import com.hipi.vm.LoadingObserverView


/**
 * @author manjiale
 * mvvm activity
 */
abstract class BaseVmActivity: BaseFrameActivity(),LoadingObserverView {

    override fun init() {
        observeLiveData()
        initViewData()
    }

    /**
     * 订阅vm
     */
    abstract fun observeLiveData()

    abstract fun initViewData()

}