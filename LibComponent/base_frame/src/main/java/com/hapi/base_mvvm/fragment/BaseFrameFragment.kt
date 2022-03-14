package com.hapi.base_mvvm.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseFrameFragment : Fragment() {

    abstract fun getLayoutId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(getLayoutId(), container, false)
        return v

    }
    abstract fun showLoading(toShow: Boolean)

}