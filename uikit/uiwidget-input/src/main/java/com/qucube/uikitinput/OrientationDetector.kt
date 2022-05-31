package com.qucube.uikitinput

import android.app.Activity
import android.provider.Settings
import android.view.OrientationEventListener
import android.view.Surface
import android.util.Log


class OrientationDetector
    (
    /**
     * 横竖屏activity
     */
    private val mActivity: Activity, val call:(mOrientation:Int)->Unit
) : OrientationEventListener(mActivity) {


    private var lastDisplayRotation:Int
    init {
        enable()
        lastDisplayRotation = displayRotation
    }

    /**
     * 获取当前屏幕旋转角度
     *
     * @return
     *
     * 0 - 表示是竖屏  90 - 表示是左横屏(正向)  180 - 表示是反向竖屏  270表示是右横屏（反向）
     */
    val displayRotation: Int
        get() {

            val rotation = mActivity.windowManager.defaultDisplay.rotation
            when (rotation) {
                Surface.ROTATION_0 -> return 0
                Surface.ROTATION_90 -> return 90
                Surface.ROTATION_180 -> return 180
                Surface.ROTATION_270 -> return 270
            }
            return 0
        }

    var isEnable = true


    /**
     * 实时记录用户手机屏幕的位置
     */
    private var mOrientation = -1
    override fun onOrientationChanged(orientation: Int) {
        //判null
        if (mActivity == null || mActivity.isFinishing) {
            return
        }
        //如果用户锁定了屏幕，不再开启代码自动旋转了，直接return
        if (!isEnable) {
            return
        }
        //记录用户手机上一次放置的位置
        val mLastOrientation = mOrientation
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            //手机平放时，检测不到有效的角度
            return
        }
        /**
         * 只检测是否有四个角度的改变
         */
        if(orientation > 350 || orientation< 10) {
            //0度，用户竖直拿着手机
            mOrientation = 0

        } else if(orientation in 81..99) {
            //90度，用户右侧横屏拿着手机
            mOrientation = 90

        } else if(orientation in 171..189) {
            //180度，用户反向竖直拿着手机
            mOrientation = 180

        } else if(orientation > 260 && orientation < 280) {
            //270度，用户左侧横屏拿着手机
            mOrientation = 270
        }
        //如果用户关闭了手机的屏幕旋转功能，不再开启代码自动旋转了，直接return
        try {
            /**
             * 1 手机已开启屏幕旋转功能
             * 0 手机未开启屏幕旋转功能
             */
            if (Settings.System.getInt(mActivity.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 0) {
                return
            }
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        Log.d("onOrientationChanged" , " displayRotation "+displayRotation+"     lastDisplayRotation "+lastDisplayRotation)
        if (displayRotation!=lastDisplayRotation) {
            lastDisplayRotation = displayRotation
            call.invoke(displayRotation)
        }
    }

}

