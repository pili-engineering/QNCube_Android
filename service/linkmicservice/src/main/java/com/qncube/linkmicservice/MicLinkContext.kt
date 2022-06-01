package com.qncube.linkmicservice

import com.nucube.rtclive.DefaultExtQNClientEventListener
import com.nucube.rtclive.RtcLiveRoom
import com.qiniu.jsonutil.JsonUtils
import java.util.*

class MicLinkContext {

    val allLinker = LinkedList<QNMicLinker>()

    fun removeLinker(uid: String): QNMicLinker?{
        getMicLinker(uid)?.let {
            allLinker.remove(it)
            return it
        }
        return null
    }

    fun addLinker(linker: QNMicLinker): Boolean {
        val it = getMicLinker(linker.user.userId)
        if (it == null) {
            allLinker.add(linker)
            return true
        } else {
            it.user = linker.user
            it.userRoomId = linker.userRoomId
            it.isOpenMicrophone = linker.isOpenMicrophone
            it.isOpenCamera = linker.isOpenCamera
            it.extensions = linker.extensions
            return false
        }

    }

    val mMicLinkerListeners = LinkedList<QNLinkMicService.MicLinkerListener>()

    fun getMicLinker(uid: String): QNMicLinker? {
        allLinker.forEach {
            if (it.user?.userId == uid) {
                return it
            }
        }
        return null
    }

    val mExtQNClientEventListener = object : DefaultExtQNClientEventListener {
        //
        override fun onUserJoined(p0: String, p1: String?) {
            val micLinker = JsonUtils.parseObject(p1, QNMicLinker::class.java) ?: return
            val it = getMicLinker(p0)
            addLinker(micLinker)
            if (it == null) {
                mMicLinkerListeners.forEach {
                    it.onUserJoinLink(micLinker)
                }
            }
        }

        override fun onUserLeft(p0: String) {
            val mic = getMicLinker(p0)
            if (mic != null) {
                removeLinker(p0)
                mMicLinkerListeners.forEach {
                    it.onUserLeft(mic)
                }
            }


        }
    }
    lateinit var mRtcLiveRoom: RtcLiveRoom


}