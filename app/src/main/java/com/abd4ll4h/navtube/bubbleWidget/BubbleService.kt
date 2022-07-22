package com.abd4ll4h.navtube.bubbleWidget

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.abd4ll4h.navtube.MainActivity
import com.abd4ll4h.navtube.R


class BubbleService : Service() {
    companion object {
        lateinit var instance: BubbleService
        var initialized = false
    }

    lateinit var windowManager: WindowManager
    lateinit var bubbleLayout: BubbleLayout
    override fun onCreate() {
        super.onCreate()


        instance = this
        initialized = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        bubbleLayout = BubbleLayout(windowManager, this)
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("Bubble Service", "NavTube Bubble")
            } else {
                ""
            }

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
        Log.i("check@bubble", "service Started")
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    internal class CheckRunningActivity(con: Context?) : Thread() {
        var am: ActivityManager? = null
        var context: Context? = null
        override fun run() {
            Looper.prepare()
            while (true) {
                // Return a list of the tasks that are currently running,
                // with the most recent being first and older ones after in order.
                // Taken 1 inside getRunningTasks method means want to take only
                // top activity from stack and forgot the olders.
                val taskInfo = am!!.getRunningTasks(1)
                val currentRunningActivityName = taskInfo[0].topActivity!!.className
                if (currentRunningActivityName == "PACKAGE_NAME.ACTIVITY_NAME") {
                    // show your activity here on top of PACKAGE_NAME.ACTIVITY_NAME
                }
            }
            Looper.loop()
        }

        init {
            context = con
            am = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        bubbleLayout.postDelayed({
            bubbleLayout.bubbleLayoutContent.springX.springConfig = SpringConfigs.NOT_DRAGGING
            bubbleLayout.bubbleLayoutContent.springY.springConfig = SpringConfigs.NOT_DRAGGING
            bubbleLayout.bubbleLayoutContent.springX.endValue=0.0
            bubbleLayout.bubbleLayoutContent.springX.endValue=0.0
            bubbleLayout.close.hide()
        },1000)
    }
}
