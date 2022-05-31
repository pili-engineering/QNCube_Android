package com.qncube.uikitcore

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

interface KitContext {
    var androidContext: Context
    var fm: FragmentManager
    var currentActivity: FragmentActivity
}