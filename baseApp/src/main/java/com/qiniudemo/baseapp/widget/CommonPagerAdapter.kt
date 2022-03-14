package com.qiniudemo.baseapp.widget

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.appcompat.widget.DialogTitle
import java.util.ArrayList

class CommonPagerAdapter(private val fragmentsList: List<Fragment>, mFm: FragmentManager, var titles:List<String>?=null) : FragmentStatePagerAdapter(mFm) {
    override fun getCount(): Int {
        return fragmentsList.size
    }

    override fun getItem(arg0: Int): Fragment {
        return fragmentsList[arg0]
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        titles?.let {
            return it[position]
        }
        return super.getPageTitle(position)
    }
}