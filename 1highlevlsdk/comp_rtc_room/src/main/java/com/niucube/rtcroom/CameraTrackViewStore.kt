package com.niucube.rtcroom

import android.view.View
import com.qiniu.droid.rtc.QNTextureView

class CameraTrackViewStore {


    //用户提前设置的摄像头窗口 ，还没有轨道绑定，稍后对方轨道发布后绑定
    val mUserUnbindVideoWindowMap = HashMap<String, QNTextureView>()
    val mUserBindedVideoWindowMap = HashMap<String, QNTextureView>()

    fun clear(){
        mUserUnbindVideoWindowMap.clear()
        mUserBindedVideoWindowMap.clear()
    }
    fun removeUserView(uid: String) {
        mUserUnbindVideoWindowMap.remove(uid)
        mUserBindedVideoWindowMap.remove(uid)
    }

    fun put2UnbindMap(uid: String, view: QNTextureView) {
        mUserUnbindVideoWindowMap[uid] = view
    }

    fun put2BindedMap(uid: String, view: QNTextureView) {
        mUserBindedVideoWindowMap[uid] = view
    }

    fun move2UnbindMap(uid: String) {
        mUserBindedVideoWindowMap.remove(uid)?.let {
            mUserUnbindVideoWindowMap[uid] = it
        }
    }

    fun move2BindedMap(uid: String) {
        mUserUnbindVideoWindowMap.remove(uid)?.let {
            mUserBindedVideoWindowMap[uid] = it
        }
    }

    fun reStoreTracksToPuller() {
        mUserBindedVideoWindowMap.entries.forEach {
            it.value.visibility = View.GONE
            mUserUnbindVideoWindowMap[it.key] = it.value
        }
        mUserBindedVideoWindowMap.clear()
    }

    fun reStoreTracksToRTC() {
        mUserUnbindVideoWindowMap.entries.forEach {
            it.value.visibility = View.VISIBLE
        }
        mUserBindedVideoWindowMap.entries.forEach {
            it.value.visibility = View.VISIBLE
        }
    }


}