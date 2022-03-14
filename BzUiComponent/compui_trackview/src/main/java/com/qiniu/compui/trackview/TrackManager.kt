package com.qiniu.compui.trackview

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import java.util.*
import kotlin.collections.ArrayList


/**
 * 轨道控制
 */
open class TrackManager<T> : RoomLifecycleMonitor {

    private val trackViews = ArrayList<TrackView<T>>()
    private val trackModeQueue = LinkedList<T>()

    /**
     * 礼物轨道view
     * 把ui上轨道view attach上来
     */
    fun addTrackView(trackView: TrackView<T>) {
        trackViews.add(trackView)
        trackView.finishedCall = {
           Handler( Looper.getMainLooper())
               .post {
                   Log.d("TrackManager", "  finishedCall  (gi ${trackModeQueue.size}")
                   checkNext()
               }
        }
    }

    private fun checkNext() {
        Log.d("TrackManager", "  checkNext  (gi ${trackModeQueue.size}")
        val trackMode = trackModeQueue.peek()
        var deal = false
        if (trackMode != null) {
            deal = dealOne(trackMode)
            if (!deal) {
                Log.d("TrackManager", "  没有处理  (gi ${trackModeQueue.size}")
            } else {
                Log.d("TrackManager", "  处理啦  (gi ${trackModeQueue.size}")
                trackModeQueue.pop()
            }
        }
    }

    private fun dealOne(trackMode: T):Boolean{
        var deal = false
        trackViews.forEach {
            if (it.isShow()) {
                //如果在处理同一个礼物
                if (it.showInSameTrack(trackMode)) {
                    it.onNewModel(trackMode)
                    deal = true
                    Log.d("TrackManager", "在处理同一个礼物")
                    return@forEach
                }
            }
        }
        //是否有空闲的轨道
        if (!deal) {
            trackViews.forEach {
                if (!deal && !it.isShow()) {
                    it.onNewModel(trackMode)
                    deal = true
                    Log.d("TrackManager", "空闲礼物")
                    return@forEach
                }
            }
        }
        return deal
    }

    /**
     * 忘轨道上添加新礼物
     */
    fun onNewTrackArrive(trackMode: T) {
        Log.d("TrackManager", "onNewTrackArrive")
        trackModeQueue.add(trackMode)
        checkNext()
    }

    override fun onRoomLeft(roomEntity: RoomEntity?) {
        super.onRoomLeft(roomEntity)
        trackViews.forEach {
            it.clear(true)
        }
        trackModeQueue.clear()
    }

    fun resetView() {
        trackViews.forEach {
            it.clear()
        }
        trackViews.clear()
        trackModeQueue.clear()
    }
}