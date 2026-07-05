package com.mskd.flux.features.images

import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.video.VideoFrameDecoder
import com.mskd.flux.features.images.data.AndroidImageRequestFactory
import com.mskd.flux.features.images.domain.ImageRequestFactory
import com.mskd.flux.features.images.domain.NetworkImageInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val moduleImagesAndroid = module {

    singleOf(::NetworkImageInterceptor)

    single<ImageLoader> {
        val context = androidContext()
        val networkImageInterceptor = get<NetworkImageInterceptor>()

        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.filesDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024L * 1024L)
                    .build()
            }
            .components {
                add(VideoFrameDecoder.Factory())
                add(networkImageInterceptor)
            }
            .build()
    }

    single<ImageRequestFactory> {
        AndroidImageRequestFactory(context = androidContext())
    }

}