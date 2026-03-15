package com.w57736e.yafeed.utils

import android.content.Context

object ScreenUtils {
    var screenWidthPx: Int = 0
        private set

    fun initialize(context: Context) {
        val displayMetrics = context.resources.displayMetrics
        screenWidthPx = displayMetrics.widthPixels
    }

    fun getContentImageWidth(): Int = (screenWidthPx * 0.8).toInt()
    fun getHeroImageWidth(): Int = screenWidthPx
}
