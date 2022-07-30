package com.abd4ll4h.navtube.utils

import android.content.Context
import android.util.TypedValue

fun getDimensionFromAttribute(context: Context, attr: Int): Int {
    val typedValue = TypedValue()
    return if (context.theme.resolveAttribute(attr, typedValue, true))
        TypedValue.complexToDimensionPixelSize(
            typedValue.data,
            context.resources.displayMetrics
        )
    else 0
}