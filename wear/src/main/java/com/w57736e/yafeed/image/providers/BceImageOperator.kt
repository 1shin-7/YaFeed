package com.w57736e.yafeed.image.providers

import android.net.Uri
import com.w57736e.yafeed.image.ImageOperator

class BceImageOperator(private val baseUrl: String) : ImageOperator {
    private val operations = mutableListOf<String>()

    override fun resize(width: Int, height: Int): ImageOperator {
        operations.add("resize,w_$width,h_$height")
        return this
    }

    override fun format(format: String): ImageOperator {
        operations.add("format,f_$format")
        return this
    }

    override fun build(): String {
        if (operations.isEmpty()) return baseUrl
        val paramString = "x-bce-process=image/${operations.joinToString("/")}"
        return appendQueryParam(baseUrl, paramString)
    }

    private fun appendQueryParam(url: String, paramString: String): String {
        val uri = Uri.parse(url)
        return if (uri.query.isNullOrEmpty()) {
            "$url?$paramString"
        } else {
            "$url&$paramString"
        }
    }

    companion object {
        private val pattern = Regex(".*\\.bcebos\\.com.*")
        fun matches(url: String) = pattern.matches(url)
        fun create(url: String) = BceImageOperator(url)
    }
}
