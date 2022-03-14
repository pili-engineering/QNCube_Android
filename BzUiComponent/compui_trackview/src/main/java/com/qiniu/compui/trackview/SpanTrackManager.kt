package com.qiniu.compui.trackview

import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

open class SpanTrackManager<T> {

    var trackSpan = 100L
    private val trackViews = ArrayList<TrackView<T>>()
    private val trackModeQueue = LinkedList<T>()

    /**
     * 礼物轨道view
     * 把ui上轨道view attach上来
     */
    fun addTrackView(trackView: TrackView<T>) {
        trackViews.add(trackView)
    }

    private var job: Job? = null

    private fun newJob() {
        job = GlobalScope.launch(Dispatchers.Main, start = CoroutineStart.LAZY) {
            var next = true

            while (next) {
                var giftTrackMode = trackModeQueue.peek()
                var deal = false
                if (giftTrackMode != null) {
                    trackViews.forEach {
                        if (it.isShow()) {
                            //如果在处理同一个礼物
                            if (it.showInSameTrack(giftTrackMode)) {
                                it.onNewModel(giftTrackMode)
                                deal = true

                                return@forEach
                            }
                        }
                    }

                    //是否有空闲的轨道
                    if (!deal) {
                        trackViews.forEach {
                            if (!deal && !it.isShow()) {
                                it.onNewModel(giftTrackMode)
                                deal = true

                                return@forEach
                            }
                        }
                    }

                    if (!deal) {
                        delay(trackSpan)
                    } else {
                        trackModeQueue.pop()
                        if (trackModeQueue.isEmpty()) {
                            next = false
                            job = null
                        } else {
                            delay(trackSpan)
                        }
                    }
                } else {
                    next = false
                    job = null
                }
            }
        }
    }

    /**
     * 忘轨道上添加新礼物
     */

    fun onNewTrackArrive(giftTrackMode: T) {
        trackModeQueue.add(giftTrackMode)
        if (job == null) {
            newJob()
            job?.start()
        }
    }

    private fun resetView() {
        trackViews.forEach {
            it.clear()
        }
        trackModeQueue.clear()
    }
}