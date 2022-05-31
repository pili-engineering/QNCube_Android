package com.qncube.uikitcore.ext

import android.os.Build
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.Spanned
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.qncube.liveroomcore.CoroutineScopeWrap
import kotlinx.android.synthetic.main.dialog_common_tip.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.launch
import java.lang.Exception

fun String.toHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

fun LifecycleOwner.bg(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    c: CoroutineScopeWrap.() -> Unit
) {
    lifecycleScope.launch(dispatcher) {
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

private var mLastClickTime = 0L
fun View.setDoubleCheckClickListener(call: (view: View) -> Unit) {
    this.setOnClickListener {
        val now = System.currentTimeMillis()
        if (now - mLastClickTime > 1000) {
            call.invoke(it)
        }
        mLastClickTime = now
    }
}