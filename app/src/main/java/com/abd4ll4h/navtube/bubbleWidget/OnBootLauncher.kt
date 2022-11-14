package com.abd4ll4h.navtube.bubbleWidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build




class OnBootLauncher: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(context, BubbleService::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startForegroundService(intent)
        } else {
            val intent = Intent(context, BubbleService::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startService(intent)
        }
    }
}