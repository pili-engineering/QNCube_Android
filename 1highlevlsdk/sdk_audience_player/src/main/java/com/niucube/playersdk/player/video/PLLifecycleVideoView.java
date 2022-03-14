package com.niucube.playersdk.player.video;


import android.content.Context;
import android.util.AttributeSet;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * 绑定activity生命周期的播放器
 */
public class PLLifecycleVideoView extends PLHappyVideoPlayer implements LifecycleObserver {

    /**
     * onpause暂停了播放
     */
    private Boolean isNeedReplay = false;

    public PLLifecycleVideoView(Context context) {
        super(context);
    }

    public PLLifecycleVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)

    public void onResume() {
        if (isNeedReplay && isPaused()) {
            resume();
            isNeedReplay = false;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        releasePlayer();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void pause(LifecycleOwner lifecycleOwner) {
        // disconnect if connected
        if (isPlaying()) {
            pause();
            isNeedReplay = true;
        }
    }

}
