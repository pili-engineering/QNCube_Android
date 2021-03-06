package com.niucube.rtclogview

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.qnrtcsdk.ExtQNClientEventListener
import com.niucube.qnrtcsdk.RtcEngineWrap
import com.qiniu.droid.rtc.*

class RTCLogView : FrameLayout {

    private var btnShow: View? = null //by lazy { findViewById<View>(R.id.log_shown_button) }

    private var mLocalTextViewForVideo: TextView? = null
    private var mLocalTextViewForAudio: TextView? = null
    private var mRemoteTextView: TextView? = null
    private val mRemoteLogText: java.lang.StringBuffer = StringBuffer()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.view_rtc_log, this, false)
        addView(view)
        btnShow = findViewById<View>(R.id.log_shown_button)

        mLocalTextViewForVideo = findViewById<TextView>(R.id.local_log_text_video)
        mLocalTextViewForVideo!!.movementMethod = ScrollingMovementMethod.getInstance()
        mLocalTextViewForAudio = findViewById<TextView>(R.id.local_log_text_audio)
        mLocalTextViewForAudio!!.movementMethod = ScrollingMovementMethod.getInstance()
        mRemoteTextView = findViewById<TextView>(R.id.remote_log_text)
        val llLogText: View = findViewById(R.id.log_text)
        btnShow?.setOnClickListener {
            if (llLogText.visibility == View.GONE) {
                llLogText.visibility = View.VISIBLE
            } else {
                llLogText.visibility = View.GONE
            }
        }
    }

    private fun updateRemoteLogText(logText: String) {
        mRemoteTextView?.text = mRemoteLogText.append(
            """
                $logText
                
                """.trimIndent()
        )
    }

    private val mExtQNClientEventListener = object : ExtQNClientEventListener {
        override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
            updateRemoteLogText("onLocalPublished ")
        }

        override fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>) {
            updateRemoteLogText("onLocalUnpublished:")
        }

        override fun onConnectionStateChanged(
            p0: QNConnectionState?,
            p1: QNConnectionDisconnectedInfo?
        ) {
            updateRemoteLogText("onConnectionStateChanged: ${p0?.name}")
        }

        override fun onUserJoined(remoteUserID: String?, userData: String?) {
            updateRemoteLogText("onRemoteUserJoined:remoteUserId = $remoteUserID ,userData = $userData")
        }

        override fun onUserReconnecting(p0: String?) {

        }

        override fun onUserReconnected(p0: String?) {
        }

        override fun onUserLeft(remoteUserID: String?) {
            updateRemoteLogText("onRemoteUserLeft:remoteUserId = $remoteUserID")
        }

        override fun onUserPublished(remoteUserID: String?, p1: MutableList<QNRemoteTrack>?) {
            updateRemoteLogText("onRemotePublished:remoteUserId = $remoteUserID")
        }

        override fun onUserUnpublished(remoteUserID: String?, p1: MutableList<QNRemoteTrack>?) {
            updateRemoteLogText("onRemoteUnpublished:remoteUserId = $remoteUserID")
        }

        override fun onSubscribed(
            remoteUserID: String?,
            remoteAudioTracks: MutableList<QNRemoteAudioTrack>?,
            remoteVideoTracks: MutableList<QNRemoteVideoTrack>?
        ) {
            updateRemoteLogText("onSubscribed:remoteUserId = $remoteUserID")

        }

        override fun onMessageReceived(p0: QNCustomMessage?) {
        }

        override fun onMediaRelayStateChanged(p0: String?, p1: QNMediaRelayState?) {
        }

    }

    private var mQNRTCClient: QNRTCClient? = null

    private fun QNLocalVideoTrackStats.toLog(): String {

        return "??????profile::" + this.profile +
                "\n ????????????=" + this.uplinkFrameRate +
                "\n ????????????=" + this.uplinkBitrate +
                "\n ?????? rtt=" + this.uplinkRTT +
                "\n ?????????=" + this.uplinkLostRate
    }

    private fun QNLocalAudioTrackStats.toLog(): String {
        return " ????????????" + this.uplinkBitrate +
                "\n ??????RTT=" + this.uplinkRTT +
                "\n ?????????=" + this.uplinkLostRate + '}';
    }

    private val mRegulatorJob = Scheduler(3000) {
        mQNRTCClient?.localVideoTrackStats?.let {
            val sb = StringBuilder()
            it.entries.forEach {
                sb.append("????????????${it.key}???\n")
                it.value.forEach {
                    sb.append(it.toLog())
                }
            }
            mLocalTextViewForVideo?.text = sb.toString()
        }
        mQNRTCClient?.localAudioTrackStats?.let {
            val sb = StringBuilder()
            it.entries.forEach {
                sb.append("????????????${it.key}???\n")
                sb.append(it.value.toLog())
            }
            mLocalTextViewForAudio?.text = sb.toString()
        }
    }

    private val roomLifecycleCallbacks = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            mRegulatorJob.start()
        }

        override fun onRoomClosed(roomEntity: RoomEntity?) {
            super.onRoomClosed(roomEntity)
            mRegulatorJob.cancel()
        }
    }


    fun attachRTCClient(rtcClient: RtcEngineWrap) {
        mQNRTCClient = rtcClient.mClient
        rtcClient.addExtraQNRTCEngineEventListener(mExtQNClientEventListener)
        RoomManager.addRoomLifecycleMonitor(roomLifecycleCallbacks)
    }
}