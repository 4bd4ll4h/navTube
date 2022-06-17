package com.abd4ll4h.navtube.bubbleWidget

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.view.*
import android.content.IntentFilter

import android.graphics.Color
import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.abd4ll4h.navtube.MainActivity
import com.abd4ll4h.navtube.R

class OverlayService : Service() {
    companion object {
        lateinit var instance: OverlayService
        var initialized = false
    }



    lateinit var windowManager: WindowManager
    lateinit var chatHeads: ChatHeads



    private lateinit var innerReceiver: InnerReceiver

    override fun onCreate() {
        super.onCreate()

        instance = this
        initialized = true

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        chatHeads = ChatHeads(this)
        Log.i("check@bubble","service Started0")
        innerReceiver = InnerReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(innerReceiver, intentFilter)

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("overlay_service", "Discord Chat Heads service")
            } else { ""}

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setContentTitle("NavTube bubble widget")
            .setSmallIcon(R.drawable.ic_nav_small_logo)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent).build()

        startForeground(101, notification)
        Log.i("check@bubble","service Started")

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onDestroy() {
        initialized = false
        unregisterReceiver(innerReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        chatHeads.add()


        Log.i("check@bubble","service Startedccc")
        return START_STICKY
    }
}

internal class InnerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS == action) {
            val reason = intent.getStringExtra("reason")
            if (reason != null) {
                OverlayService.instance.chatHeads.collapse()
            }
        }
    }
}