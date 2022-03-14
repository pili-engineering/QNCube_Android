package com.hapi.base_mvvm.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hapi.base_mvvm.fragment.BaseFrameFragment
import com.hipi.vm.LoadingObserverView

abstract class BaseVmFragment: BaseFrameFragment() ,LoadingObserverView{

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        observeLiveData()
        initViewData()
    }
    abstract fun observeLiveData()
    abstract fun initViewData()
}