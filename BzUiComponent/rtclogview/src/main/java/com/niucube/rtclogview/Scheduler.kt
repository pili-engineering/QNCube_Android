
package com.niucube.rtclogview

import kotlinx.coroutines.*

class Scheduler(
    private val delayTimeMillis: Long,
    private val coroutineScope: CoroutineScope = GlobalScope,
    private val action: suspend CoroutineScope.() -> Unit
) {
    private var job: Job? = null

    fun start() {
        job = coroutineScope.launch(Dispatchers.Main) {
            while (true) {
                delay(delayTimeMillis)
                launch { action() }
            }
        }
    }

    fun cancel() {
        job?.cancel()
    }
}