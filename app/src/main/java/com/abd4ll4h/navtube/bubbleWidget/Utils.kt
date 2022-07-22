package com.abd4ll4h.navtube.bubbleWidget

import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import android.util.TypedValue

fun getOverlayFlag(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_PHONE
    }
}

fun getScreenSize(): DisplayMetrics {
    return Resources.getSystem().displayMetrics
}

fun dpToPx(dp: Float): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun spToPx(sp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().displayMetrics)
}

fun runOnMainLoop(fn: () -> Unit) {
    Handler(Looper.getMainLooper()).post {
        fn()
    }
}



fun debug(txt: String) {
    println("asdf: $txt")
}
class Rectangle(val x: Double, val y: Double, val w: Double, val h: Double) {
    private val OUT_LEFT = 1
    private val OUT_TOP = 2
    private val OUT_RIGHT = 4
    private val OUT_BOTTOM = 8

    fun outcode(x: Double, y: Double): Int {
        var out = 0

        when {
            w <= 0 -> out = out or (OUT_LEFT or OUT_RIGHT)
            x < this.x -> out = out or OUT_LEFT
            x > this.x + w -> out = out or OUT_RIGHT
        }
        when {
            h <= 0 -> out = out or (OUT_TOP or OUT_BOTTOM)
            y < this.y -> out = out or OUT_TOP
            y > this.y + h -> out = out or OUT_BOTTOM
        }
        return out
    }

    fun intersectsLine(x1: Double, y1: Double, x2: Double, y2: Double): Boolean {
        var x1 = x1
        var y1 = y1
        var out1: Int
        val out2: Int = outcode(x2, y2)
        if (out2 == 0) {
            return true
        }
        do {
            out1 = outcode(x1, y1)

            if (out1 == 0) break

            if (out1 and out2 != 0) {
                return false
            }
            if (out1 and (OUT_LEFT or OUT_RIGHT) != 0) {
                var x = x
                if (out1 and OUT_RIGHT != 0) {
                    x += w
                }
                y1 += (x - x1) * (y2 - y1) / (x2 - x1)
                x1 = x
            } else {
                var y = y
                if (out1 and OUT_BOTTOM != 0) {
                    y += h
                }
                x1 += (y - y1) * (x2 - x1) / (y2 - y1)
                y1 = y
            }
        } while (true)

        return true
    }
}

class Line(val x1: Double, val y1: Double, var x2: Double, var y2: Double) {
    fun intersects(r: Rectangle): Boolean {
        return r.intersectsLine(x1, y1, x2, y2)
    }

    private fun f(x: Double): Double {
        val slope = (y2 - y1) / (x2 - x1)
        return y1 + (x - x1) * slope
    }

    fun changeLength(length: Double): Line {
        val newX1 = x1 - length / 2
        val newX2 = x2 - length / 2
        return Line(newX1, f(newX1), newX2, f(newX2))
    }
}