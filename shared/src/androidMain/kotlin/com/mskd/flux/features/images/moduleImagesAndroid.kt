package com.mskd.flux.features.images

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.video.VideoFrameDecoder
import com.mskd.flux.features.images.data.AndroidImageRequestFactory
import com.mskd.flux.features.images.domain.ImageRequestFactory
import com.mskd.flux.features.images.domain.NetworkImageInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Dispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

@OptIn(ExperimentalCoilApi::class)
val moduleImagesAndroid = module {

    singleOf(::NetworkImageInterceptor)

    single<MemoryCache> {
        MemoryCache.Builder()
            .maxSizePercent(androidContext(), 0.25)
            .build()
    }

    single<DiskCache> {
        DiskCache.Builder()
            .directory(androidContext().filesDir.resolve("image_cache"))
            .maxSizeBytes(512L * 1024L * 1024L)
            .build()
    }

    single(named("uiClient")) {
        HttpClient(OkHttp) {
            engine {
                config {
                    dispatcher(Dispatcher().apply { maxRequestsPerHost = 6 })
                }
            }
        }
    }

    single(named("cacheClient")) {
        HttpClient(OkHttp) {
            engine {
                config {
                    dispatcher(Dispatcher().apply { maxRequestsPerHost = 2 })
                }
            }
        }
    }

    single<ImageLoader>(named("uiImageLoader")) {
        ImageLoader.Builder(androidContext())
            .memoryCache { get<MemoryCache>() }
            .diskCache { get<DiskCache>() }
            .components {
                add(VideoFrameDecoder.Factory())
                add(get<NetworkImageInterceptor>())
                add(KtorNetworkFetcherFactory(httpClient = get<HttpClient>(named("uiClient"))))
            }
            .build()
    }

    single<ImageLoader>(named("cacheImageLoader")) {
        ImageLoader.Builder(androidContext())
            .memoryCache { get<MemoryCache>() }
            .diskCache { get<DiskCache>() }
            .components {
                add(get<NetworkImageInterceptor>())
                add(KtorNetworkFetcherFactory(httpClient = get<HttpClient>(named("cacheClient"))))
            }
            .build()
    }

    single<ImageRequestFactory> {
        AndroidImageRequestFactory(context = androidContext())
    }

}