package com.hipi.vm

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun BaseViewModel.bgDefault(
    block: suspend CoroutineScope.() -> Unit
) {
    GlobalScope.launch(Dispatchers.Main) {
        try {
            block.invoke(this)
        } catch (e: Exception) {
            e.printStackTrace()
            toast(e.message)
        } finally {

        }
    }
}

class CoroutineScopeWrap {
    var work: (suspend CoroutineScope.() -> Unit) = {}
    var error: (e: Throwable) -> Unit = {}
    var complete: () -> Unit = {}

    fun doWork(call: suspend CoroutineScope.() -> Unit) {
        this.work = call
    }

    fun catchError(error: (e: Throwable) -> Unit) {
        this.error = error
    }

    fun onFinally(call: () -> Unit) {
        this.complete = call
    }
}

fun backGround(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    c: CoroutineScopeWrap.() -> Unit
) {
    GlobalScope
        .launch(dispatcher) {
            val block = CoroutineScopeWrap()
            c.invoke(block)
            try {
                block.work.invoke(this)
            } catch (e: Exception) {
                e.printStackTrace()
                block.error.invoke(e)
            } finally {
                block.complete.invoke()
            }
        }
}

fun BaseViewModel.vmScopeBg(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    c: CoroutineScopeWrap.() -> Unit
) {
    this.viewModelScope
        .launch(dispatcher) {
            val block = CoroutineScopeWrap()
            c.invoke(block)
            try {
                block.work.invoke(this)
            } catch (e: Exception) {
                toast(e.message)
                e.printStackTrace()
                block.error.invoke(e)
            } finally {
                block.complete.invoke()
            }
        }
}


fun AppCompatActivity.lifecycleBg(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    c: CoroutineScopeWrap.() -> Unit
) {
    this.lifecycleScope.launch(dispatcher) {
        val block = CoroutineScopeWrap()
        c.invoke(block)
        try {
            block.work.invoke(this)
        } catch (e: Exception) {
            e.printStackTrace()
            this@lifecycleBg?.applicationContext?.let {
                Toast.makeText(it, e.message, Toast.LENGTH_SHORT).show()
            }
            block.error.invoke(e)
        } finally {
            block.complete.invoke()
        }
    }

}


fun AppCompatActivity.bgDefault(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
) {
    this.lifecycleScope.launch(dispatcher) {
        try {
            block.invoke(this)
        } catch (e: Exception) {
            e.printStackTrace()
            this@bgDefault?.applicationContext?.let {
                Toast.makeText(it, e.message, Toast.LENGTH_SHORT).show()
            }
        } finally {

        }
    }
}


fun Fragment.lifecycleBg(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    c: CoroutineScopeWrap.() -> Unit
) {
    this.lifecycleScope.launch(dispatcher) {
        val block = CoroutineScopeWrap()
        c.invoke(block)
        try {
            block.work.invoke(this)
        } catch (e: Exception) {
            requireContext().applicationContext?.let {
                Toast.makeText(it, e.message, Toast.LENGTH_SHORT).show()
            }
            block.error.invoke(e)
        } finally {
            block.complete.invoke()
        }
    }
}

fun Fragment.bgDefault(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
) {
    this.lifecycleScope.launch(dispatcher) {
        try {
            block.invoke(this)
        } catch (e: Exception) {
            e.printStackTrace()
            requireContext()?.applicationContext?.let {
                Toast.makeText(it, e.message, Toast.LENGTH_SHORT).show()
            }
        } finally {

        }
    }
}


