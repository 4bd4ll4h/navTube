package layout


import android.graphics.*
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.bubbleWidget.*
import com.facebook.rebound.SpringSystem

class CloseView(var layout: BubbleLayout): View(layout.context1) {


    private var gradientParams = FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dpToPx(150f))
    var springSystem = SpringSystem.create()

    var springY = springSystem.createSpring()
    var springX = springSystem.createSpring()
    var springAlpha = springSystem.createSpring()
    var springScale = springSystem.createSpring()

    val paint = Paint()

    val gradient = FrameLayout(context)

    var hidden = true


    fun hide() {
        val metrics = getScreenSize()
        springY.endValue = metrics.heightPixels.toDouble() + height
        springX.endValue = metrics.widthPixels.toDouble() / 2 - width / 2

        springAlpha.endValue = 0.0
        hidden = true
    }
    fun show() {
        hidden = false
        visibility = View.VISIBLE

        springAlpha.endValue = 1.0
        resetScale()
    }
    fun resetScale() {
        springScale.endValue = 1.0
    }

    init {
        this.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

        visibility = View.INVISIBLE
        hide()

        springScale.springConfig = SpringConfigs.CLOSE_SCALE
        springY.springConfig = SpringConfigs.CLOSE_Y


        gradientParams.gravity = Gravity.BOTTOM

        gradient.background = ContextCompat.getDrawable(context, R.drawable.gradient_bg)
        springAlpha.currentValue = 0.0

        z = 100f


    }

    override fun onDraw(canvas: Canvas?) {
    }
}