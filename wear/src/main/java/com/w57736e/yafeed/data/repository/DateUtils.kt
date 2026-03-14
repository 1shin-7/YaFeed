package com.w57736e.yafeed.data.repository

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    // Format: MM/dd HH:mm
    private val displayFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    
    // Most common RSS formats: 
    // "EEE, dd MMM yyyy HH:mm:ss Z" (RFC 822)
    // "yyyy-MM-dd'T'HH:mm:ssXXX" (ISO 8601)
    private val rfc822Format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)

    fun formatRssDate(dateStr: String?): String {
        if (dateStr == null) return ""
        
        val date = try {
            rfc822Format.parse(dateStr) ?: iso8601Format.parse(dateStr)
        } catch (e: Exception) {
            null
        }

        return if (date != null) {
            displayFormat.format(date)
        } else {
            // If parsing fails, just return the string or a truncated version
            dateStr.take(16)
        }
    }
}
