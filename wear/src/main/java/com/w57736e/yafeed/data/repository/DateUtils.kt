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

        return null
    }

    fun parseToTimestamp(dateStr: String?): Long? {
        return parseToZonedDateTime(dateStr)?.toInstant()?.toEpochMilli()
    }

    fun formatRssDate(timestamp: Long?): String {
        if (timestamp == null) return ""
        val zdt = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault())
        return zdt.format(displayFormatter)
    }

    fun formatRssDateFull(timestamp: Long?): String {
        if (timestamp == null) return ""
        val zdt = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault())
        return zdt.format(fullDisplayFormatter)
    }
}
