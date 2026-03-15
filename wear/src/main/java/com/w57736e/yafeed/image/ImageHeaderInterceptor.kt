package com.w57736e.yafeed.image

import okhttp3.Interceptor
import okhttp3.Response

class ImageHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .build()
        return chain.proceed(request)
    }
}
