package com.qiniu.bzuicomp.gift;

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.*
import kotlin.collections.ArrayList

class BigGiftManager<T> : LifecycleObserver {

    private var queueBigGiftView = ArrayList<IBigGiftView<T>>()

    private val giftTrackModeQueue = LinkedList<T>()

    fun attch(view: IBigGiftView<T>) {
        this.queueBigGiftView.add(view)
        view.finishedCall = {
            val head =
                giftTrackModeQueue.peek()
            if (head != null) {
                var deal = false
                queueBigGiftView.forEach { v ->
                    if (v.playIfPlayAble(head)) {
                        deal = true
                        return@forEach
                    }
                }
                if (deal) {
                    giftTrackModeQueue.poll()
                }
            }
        }
    }

    /**
     * 排队播放的大动画
     */
    fun playInQueen(bigAnimalMode: T) {
        var deal = false
        queueBigGiftView.forEach {
            if (!it.isPlaying){
                if (it.playIfPlayAble(bigAnimalMode)) {
                    deal = true
                    return@forEach
                }
            }
        }
        if (!deal) {
            giftTrackModeQueue.add(bigAnimalMode)
        }
    }

    /**
     * 不排队马上播放
     * newBigGiftView :构造一个新BigGiftView 我来控制播放
     */
    fun playNow(bigContainer: ViewGroup,bigAnimalMode: T, newBigGiftView: IBigGiftView<T>) {
        bigContainer.addView(newBigGiftView.getView())
        newBigGiftView.finishedCall = {
            bigContainer.removeView(newBigGiftView.getView())
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        resetView()
    }

    fun resetView() {
        queueBigGiftView.forEach {
            it.clear()
        }
        queueBigGiftView.clear()
        giftTrackModeQueue.clear()
    }
}

interface IBigGiftView<T> {

    var finishedCall: (() -> Unit)?
    var isPlaying: Boolean
    fun getView(): View
    fun clear()

    /**
     * 能
     */
    fun playIfPlayAble(gigGiftMode: T): Boolean
}