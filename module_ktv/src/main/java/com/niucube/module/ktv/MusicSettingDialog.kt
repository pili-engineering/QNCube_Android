package com.niucube.module.ktv

import android.view.Gravity
import android.widget.SeekBar
import com.hipi.vm.activityVm
import com.niucube.module.ktv.databinding.DialogMusicSettingBinding
import com.niucube.qrtcroom.ktvkit.TrackType
import com.qiniudemo.baseapp.BaseDialogFragment

class MusicSettingDialog : BaseDialogFragment<DialogMusicSettingBinding>() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    private val ktvRoomVm by activityVm<KTVRoomVm>()
    override fun initViewData() {
        binding.sbVol1.progress = ktvRoomVm.mKTVPlayerKit.getMicrophoneVolume()
        binding.sbVol2.progress = ktvRoomVm.mKTVPlayerKit.getMusicVolume()
        binding.sbVol1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                ktvRoomVm.mKTVPlayerKit.setMicrophoneVolume(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        binding.sbVol2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                ktvRoomVm.mKTVPlayerKit.setMusicVolume(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        binding.switchEar.isChecked =
            (ktvRoomVm.mKTVPlayerKit.mKTVMusic?.trackType == TrackType.originVoice.value)

        binding.switchEar.setOnCheckedChangeListener { _, b ->
            // ktvRoomVm.mKTVPlayerKit.switchTrack()
            if (b) {
                ktvRoomVm.mKTVPlayerKit.switchTrack(TrackType.originVoice)
            } else {
                ktvRoomVm.mKTVPlayerKit.switchTrack(TrackType.accompany)
            }
        }
    }
}