package com.abd4ll4h.navtube.bubbleWidget


import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Handler
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.Nullable
import com.abd4ll4h.navtube.R


/**
 * Created by karthikrk on 04/09/15.
 */
class Content : Service() {
    private var windowManager: WindowManager? = null
    private var removeView: RelativeLayout? = null

    /**our custom chat head view */
    private var chatheadView: ChatHead? = null
    private val szWindow = Point()
    private var x_remove = 0f
    private var y_remove = 0f
    private var screenHeight = 0f
    private var screenWidth = 0f
    private var statusBarHeight = 0f
    private var mContext: Context? = null
    private var removeImg: ImageView? = null
    private var displayMetrics: DisplayMetrics? = null
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e("TAG", "CHAT HEAD SERVICE STARTED")
        displayMetrics = resources.displayMetrics
        screenHeight = displayMetrics!!.heightPixels.toFloat()
        screenWidth = displayMetrics!!.widthPixels.toFloat()
        statusBarHeight = getStatusBarHeight()
        return if (startId == START_STICKY) {
            displayChatBubble()
            super.onStartCommand(intent, flags, startId)
        } else {
            START_NOT_STICKY
        }
    }

    private fun displayChatBubble() {
        mContext = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = (mContext as Content).getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater


        //Adding the remove view
        removeView = inflater.inflate(R.layout.bubble, null) as RelativeLayout?
        val removeParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getOverlayFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        //in order to add the remove exactly at the bottom of the screen
        removeParams.gravity = Gravity.BOTTOM or Gravity.CENTER
        removeView!!.visibility = View.GONE
        removeImg = removeView!!.findViewById<View>(R.id.remove_img) as ImageView
        windowManager!!.addView(removeView, removeParams)
        chatheadView = ChatHead(mContext)
        windowManager!!.defaultDisplay.getSize(szWindow)

        //use the same params as that of remove view just change the gravity
        val chatHeadParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getOverlayFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        windowManager!!.addView(chatheadView, chatHeadParams)
        chatheadView!!.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                var xPos = event.rawX
                var yPos = event.rawY
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        handler_longClick.post(runnable_longClick)
                        remove_img_width = removeImg!!.layoutParams.width
                        remove_img_height = removeImg!!.layoutParams.height
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (xPos + chatheadView!!.mBitmap.width > screenWidth) {
                            xPos = screenWidth - chatheadView!!.mBitmap.width
                        } else if (xPos < 0) {
                            xPos = 0f
                        }
                        if (yPos + chatheadView!!.mBitmap.height + statusBarHeight > screenHeight) {
                            yPos = screenHeight - statusBarHeight - chatheadView!!.mBitmap.height
                        } else if (yPos < 0) {
                            yPos = 0f
                        }
                        chatheadView!!.mXSprings!!.endValue = xPos.toDouble()
                        chatheadView!!.mYSprings!!.endValue = yPos.toDouble()
                        val normalRemoveHeight = pixels
                        if (yPos == screenHeight - statusBarHeight - chatheadView!!.mBitmap.height ||
                            (yPos < screenHeight - statusBarHeight - chatheadView!!.mBitmap.height
                                    && yPos >= y_remove - removeView!!.height - chatheadView!!.mBitmap.height)
                        ) {
                            if (checkViewIntersection(xPos.toInt())) {
                                removeImg!!.layoutParams.height = (remove_img_height * 1.5).toInt()
                                removeImg!!.layoutParams.width = (remove_img_width * 1.5).toInt()
                                windowManager!!.updateViewLayout(
                                    removeView,
                                    removeView!!.layoutParams
                                )
                                inBound = true
                            } else {
                                removeImg!!.layoutParams.height = normalRemoveHeight
                                removeImg!!.layoutParams.width = normalRemoveHeight
                                windowManager!!.updateViewLayout(
                                    removeView,
                                    removeView!!.layoutParams
                                )
                                inBound = false
                            }
                        } else {
                            //restore the screenHeight to the normal of the remove view
                            if (inBound) {
                                removeImg!!.layoutParams.height = normalRemoveHeight
                                removeImg!!.layoutParams.width = normalRemoveHeight
                                windowManager!!.updateViewLayout(
                                    removeView,
                                    removeView!!.layoutParams
                                )
                                inBound = false
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        removeView!!.visibility = View.GONE
                        removeImg!!.layoutParams.height = remove_img_height
                        removeImg!!.layoutParams.width = remove_img_width
                        handler_longClick.removeCallbacks(runnable_longClick)
                        if (inBound && chatheadView != null) {
                            //remove the chathead view here
                            windowManager!!.removeView(chatheadView)
                            stopSelf()
                            chatheadView = null
                        }
                    }
                    else -> Log.e(
                        "TAG",
                        "chatheadView.setOnTouchListener  -> event.getAction() : default"
                    )
                }
                return true
            }

            var inBound = false
            var remove_img_width = 0
            var remove_img_height = 0
            var handler_longClick = Handler()
            var runnable_longClick = Runnable {
                removeView!!.visibility = View.VISIBLE
                if (x_remove == 0f || y_remove == 0f) {
                    val pos = IntArray(2)
                    removeView!!.getLocationOnScreen(pos)
                    x_remove = pos[0].toFloat()
                    y_remove = pos[1].toFloat()
                }
            }
        })
    }

    private fun getStatusBarHeight(): Float {
        var result = 0f
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId).toFloat()
        }
        return result
    }

    private fun checkViewIntersection(x: Int): Boolean {
        return if (x > x_remove - removeView!!.width && x < x_remove) true else false
    }

    private val pixels: Int
        private get() {
            val scale = displayMetrics!!.density
            return (80 * scale + 0.5f).toInt()
        }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("TAG", "CHAT HEAD SERVICE DESTROYED")
        if (chatheadView != null) {
            windowManager!!.removeView(chatheadView)
        }
        if (removeView != null) {
            windowManager!!.removeView(removeView)
        }
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}

