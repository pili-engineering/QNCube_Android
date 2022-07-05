package com.niucube.basemutableroom.screencapture


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class RecordService: Service() {

    inner class MediaProjectionBinder : Binder() {
        val service: RecordService
            get() = this@RecordService
    }

    companion object{
        /**
         * 绑定Service
         *
         * @param context           context
         * @param serviceConnection serviceConnection
         */
        fun bindService(context: Context, serviceConnection: ServiceConnection?) {
            val intent = Intent(context, RecordService::class.java)
            val bindService = context.bindService(intent, serviceConnection!!, BIND_AUTO_CREATE)
            Log.d("MediaProjectionService", "bindService $bindService")
        }

        /**
         * 解绑Service
         *
         * @param context           context
         * @param serviceConnection serviceConnection
         */
        fun unbindService(context: Context, serviceConnection: ServiceConnection?) {
            context.unbindService(serviceConnection!!)
        }

    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val intent1 = Intent()
        val intent2 = Intent()
        val pendingIntent1 = PendingIntent.getActivity(this, 0, intent1, 0)
        val pendingIntent2 = PendingIntent.getActivity(this, 0, intent2, 0)
        val notification1 =
            NotificationCompat.Builder(this, "ScreenRecorder")
                .setContentTitle("yNote studios")
                .setContentText("Filming...")
                .setContentIntent(pendingIntent1).build()
        val notification2 =
            NotificationCompat.Builder(this, "ScreenRecorder")
                .setContentTitle("yNote studios")
                .setContentText("Filming...")
                .setContentIntent(pendingIntent1).build()
        startForeground(1, notification1)
        startForeground(2, notification2)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("MediaProjectionService", "onBind ")
        return MediaProjectionBinder()
        // return mMessenger.getBinder();
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ScreenRecorder", "Foreground notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }
}