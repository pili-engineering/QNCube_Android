package com.hipi.vm

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

class LifecycleUiCall<T>(val lifecycle: LifecycleOwner, val call: (t: T) -> Unit) :
    LifecycleObserver {

    init {
        lifecycle.lifecycle.addObserver(this)
    }

    fun onFinish() {
        lifecycle.lifecycle.removeObserver(this)
    }

    fun onNext(t: T) {
        if (isDestroy) {
            return
        }
        call.invoke(t)
        onFinish()
    }

    var isDestroy = false

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        isDestroy = true
        lifecycle.lifecycle.removeObserver(this)
    }
}