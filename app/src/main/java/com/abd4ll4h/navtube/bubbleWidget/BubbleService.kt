package com.abd4ll4h.navtube.bubbleWidget

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.abd4ll4h.navtube.DataFetch.Repository
import com.abd4ll4h.navtube.MainActivity
import com.abd4ll4h.navtube.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class BubbleService : Service() {
    companion object {
        lateinit var instance: BubbleService
        lateinit var repository: Repository
        val bubbleScope = CoroutineScope(Dispatchers.IO)
        var initialized = false
    }

    lateinit var windowManager: WindowManager
    lateinit var bubbleLayout: BubbleLayout
    override fun onCreate() {
        super.onCreate()


        instance = this
        initialized = true
        repository=Repository.DataRepository.getInstance(this)

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
            .setSmallIcon(R.drawable.ic_small_logo)
            .setColor(getColor(R.color.red))
            .setColorized(true)
            .addAction(R.drawable.ic_show,getString(R.string.show),PendingIntent.getBroadcast(application,0,Intent(application, ShowBroadcast::class.java),0))
            .addAction(R.drawable.ic_hide,getString(R.string.hide),PendingIntent.getBroadcast(application,1,Intent(application, HideBroadcast::class.java),0))
            .addAction(R.drawable.ic_baseline_close_24,getString(R.string.Close),PendingIntent.getBroadcast(application,2,Intent(application, CloseBroadcast::class.java),0))
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


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        bubbleLayout.postDelayed({
            bubbleLayout.bubble.springX.springConfig = SpringConfigs.NOT_DRAGGING
            bubbleLayout.bubble.springY.springConfig = SpringConfigs.NOT_DRAGGING
            bubbleLayout.bubble.springX.endValue=0.0
            bubbleLayout.bubble.springX.endValue=0.0
            bubbleLayout.close.hide()
        },1000)
    }

     class CloseBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            instance.bubbleLayout.onClose()
            instance.stopForeground(true)
            instance.stopSelf()
        }
    }
     class ShowBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (instance.bubbleLayout.bubble.isConnection){
                instance.bubbleLayout.showAll()
            }else{
                Toast.makeText(context,context!!.getString(R.string.no_Internet),Toast.LENGTH_LONG).show()
            }

        }
    }
     class HideBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            instance.bubbleLayout.hideAll()
        }
    }
}
