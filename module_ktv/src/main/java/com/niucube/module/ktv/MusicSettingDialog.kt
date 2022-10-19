package com.niucube.module.ktv

import android.view.Gravity
import android.widget.SeekBar
import com.hipi.vm.activityVm
import com.niucube.qrtcroom.ktvkit.TrackType
import com.qiniudemo.baseapp.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_music_setting.*

class MusicSettingDialog : BaseDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    private val ktvRoomVm by activityVm<KTVRoomVm>()
    override fun initViewData() {
        sbVol1.progress = ktvRoomVm.mKTVPlayerKit.getMicrophoneVolume()
        sbVol2.progress = ktvRoomVm.mKTVPlayerKit.getMusicVolume()
        sbVol1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                ktvRoomVm.mKTVPlayerKit.setMicrophoneVolume(p1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        sbVol2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                ktvRoomVm.mKTVPlayerKit.setMusicVolume(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        switchEar.isChecked =
            (ktvRoomVm.mKTVPlayerKit.mKTVMusic?.trackType == TrackType.originVoice.value)

        switchEar.setOnCheckedChangeListener { _, b ->
            // ktvRoomVm.mKTVPlayerKit.switchTrack()
            if (b) {
                ktvRoomVm.mKTVPlayerKit.switchTrack(TrackType.originVoice)
            } else {
                ktvRoomVm.mKTVPlayerKit.switchTrack(TrackType.accompany)
            }
        }
    }

    override fun getViewLayoutId(): Int {
        return R.layout.dialog_music_setting
    }
}