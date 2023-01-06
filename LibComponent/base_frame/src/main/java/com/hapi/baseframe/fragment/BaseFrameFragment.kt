package com.hapi.baseframe.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hipi.vm.LoadingObserverView

abstract class BaseFrameFragment : Fragment(),LoadingObserverView {

    abstract fun getLayoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(getLayoutId(), container, false)
        return v

    }
}