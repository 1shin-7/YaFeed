package com.w57736e.yafeed.data.repository

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {
    // 目标显示格式
    private val displayFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm", Locale.getDefault())
    private val fullDisplayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.getDefault())

    // 预定义的解析器列表
    private val formatters = listOf(
        DateTimeFormatter.RFC_1123_DATE_TIME, // 处理 "Sat, 14 Mar 2026 09:00:15 GMT"
        DateTimeFormatter.ISO_OFFSET_DATE_TIME, // 处理 "2026-03-13T18:08:39-04:00"
        DateTimeFormatter.ISO_DATE_TIME
    )

    private fun parseToZonedDateTime(dateStr: String?): ZonedDateTime? {
        if (dateStr == null) return null
        
        // 尝试所有可能的解析器
        for (formatter in formatters) {
            try {
                return ZonedDateTime.parse(dateStr, formatter)
            } catch (e: Exception) {
                continue
            }
        }
        
        // 如果标准解析器都失败了，且包含时区偏移但没有逗号（某些非标格式）
        // 可以在这里添加自定义处理逻辑，目前先返回 null
        return null
    }

    fun formatRssDate(dateStr: String?): String {
        val zdt = parseToZonedDateTime(dateStr)
        return zdt?.format(displayFormatter) ?: dateStr?.take(16) ?: ""
    }

    fun formatRssDateFull(dateStr: String?): String {
        val zdt = parseToZonedDateTime(dateStr)
        return zdt?.format(fullDisplayFormatter) ?: dateStr?.take(19) ?: ""
    }
}
