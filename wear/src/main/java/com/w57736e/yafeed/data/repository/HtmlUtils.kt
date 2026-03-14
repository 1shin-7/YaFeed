package com.w57736e.yafeed.data.repository

import android.text.Html
import androidx.core.text.HtmlCompat

object HtmlUtils {
    /**
     * 将 HTML 字符串转换为纯文本，用于列表预览
     */
    fun stripHtml(html: String?): String {
        if (html == null) return ""
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
    }
}
