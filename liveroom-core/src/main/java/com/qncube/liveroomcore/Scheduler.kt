package com.qncube.liveroomcore

import kotlinx.coroutines.*


class Scheduler(
    private val delayTimeMillis: Long,
    private val coroutineScope: CoroutineScope = GlobalScope,
    val action: suspend CoroutineScope.() -> Unit
) {
    private var job: Job? = null

    fun start() {
        job = coroutineScope.launch(Dispatchers.Main) {
            while (true) {
                action()
                delay(delayTimeMillis)
            }
        }
    }

    fun cancel() {
        job?.cancel()
        job=null
    }
}