package com.w57736e.yafeed.image

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient

object CoilImageLoaderFactory {
    fun create(context: Context): ImageLoader {
        val maxMemory = Runtime.getRuntime().maxMemory()
        val cacheSize = (maxMemory / 4).toInt() // 25% of available memory

        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizeBytes(cacheSize)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor(ImageHeaderInterceptor())
                    .build()
            }
            .build()
    }
}
