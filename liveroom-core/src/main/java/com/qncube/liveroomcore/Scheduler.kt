package com.qncube.liveroomcore

import kotlinx.coroutines.*


class Scheduler(
    private val delayTimeMillis: Long,
    private val coroutineScope: CoroutineScope = GlobalScope,
    val action: suspend CoroutineScope.() -> Unit
) {
    private var job: Job? = null

    fun start(delayBefore:Boolean = false) {
        job = coroutineScope.launch(Dispatchers.Main) {
            while (true) {
                if(delayBefore){
                    delay(delayTimeMillis)
                    action()
                }else{
                    action()
                    delay(delayTimeMillis)
                }

            }
        }
    }

    fun cancel() {
        job?.cancel()
        job=null
    }
}