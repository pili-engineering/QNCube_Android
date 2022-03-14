package com.hipi.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.os.Bundle
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.LifecycleObserver
import kotlinx.coroutines.*
import java.lang.Exception


/**
 * @author manjiale
 *
 */
open class BaseViewModel : AndroidViewModel,LifecycleObserver {

    var mData: Bundle? = null
        private set

    /**
     * 获取activity fm
     */
    var getFragmentManagrCall: (() -> androidx.fragment.app.FragmentManager)? = null

    /**
     * 回调showloading
     */
    var showLoadingCall: ((show: Boolean) -> Unit)? = null

    /**
     * 接受activity 回调
     */
    var finishedActivityCall: (() -> Unit)? = null

    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }

    constructor(application: Application) : super(application)
    constructor(application: Application, data: Bundle?) : super(application) {
        mData = data
    }

    /**
     * 显示弹窗
     */
    fun showDialog(tag: String, call: () -> androidx.fragment.app.DialogFragment) {
        getFragmentManagrCall?.invoke()?.let {
            call().show(it, tag)
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeCall()
    }
    /**
     * 页面销毁
     */
    private fun removeCall() {
        finishedActivityCall = null
        getFragmentManagrCall = null
        showLoadingCall = null
    }

    fun getAppContext(): Application {
        return getApplication<Application>()
    }

    fun toast(@StringRes msgRes: Int) {
        Toast.makeText(getAppContext(), getAppContext().resources.getString(msgRes), Toast.LENGTH_SHORT).show()
    }

    fun toast(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(getAppContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }
}