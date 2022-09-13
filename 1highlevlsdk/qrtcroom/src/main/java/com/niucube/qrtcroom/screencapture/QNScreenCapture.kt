package com.niucube.qrtcroom.screencapture

import android.content.Intent
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.qiniu.droid.rtc.QNScreenVideoTrack

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


fun FragmentActivity.onScreenCaptureResult(requestCode: Int, resultCode: Int, data: Intent?,call:()->Unit) {
         if (requestCode == QNScreenVideoTrack.SCREEN_CAPTURE_PERMISSION_REQUEST_CODE && QNScreenVideoTrack.checkActivityResult(
                 requestCode,
                 resultCode,
                 data
             )
         ) {
             val intent = Intent(this, RecordService::class.java)
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 startForegroundService(intent)
             } else {
                 startService(intent)
             }
             GlobalScope.launch(Dispatchers.Main) {
                 delay(1000)
                 call.invoke()
             }
         }
     }

