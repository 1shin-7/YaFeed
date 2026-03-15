package com.w57736e.yafeed.image

import android.content.Context
import coil.ImageLoader
import okhttp3.OkHttpClient

object CoilImageLoaderFactory {
    fun create(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor(ImageHeaderInterceptor())
                    .build()
            }
            .build()
    }
}
