package com.example.footu

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class MyGlideModule : AppGlideModule() {
    override fun applyOptions(
        context: Context,
        builder: GlideBuilder,
    ) {
        super.applyOptions(context, builder)

        // Set disk cache size
        val diskCacheSizeBytes = 1024 * 1024 * 50L // 250 MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes))

        // Set memory cache size
        val memoryCacheSizeBytes = 1024 * 1024 * 20L // 20 MB
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes))

        // Optionally set other options like default request options
        builder.setDefaultRequestOptions(
            RequestOptions()
                .error(android.R.drawable.stat_notify_error),
        )
    }
}
