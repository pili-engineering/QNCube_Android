package com.qiniudemo.baseapp.widget

import com.qiniu.baseapp.R
import com.qiniudemo.baseapp.BaseFragment

class EmptyFragment : BaseFragment() {

    override fun initViewData() {}

    override fun getLayoutId(): Int {
      return R.layout.fragment_empty

    }
}