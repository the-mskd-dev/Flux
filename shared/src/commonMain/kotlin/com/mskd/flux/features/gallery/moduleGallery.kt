package com.mskd.flux.features.gallery

import com.mskd.flux.features.gallery.data.datasource.CoilCachedImageDataSource
import com.mskd.flux.features.gallery.data.datasource.KtorRemoteImageDataSource
import com.mskd.flux.features.gallery.domain.datasource.CachedImageDataSource
import com.mskd.flux.features.gallery.domain.datasource.RemoteImageDataSource
import com.mskd.flux.features.gallery.domain.usecase.GetGalleryImagesUseCase
import com.mskd.flux.features.gallery.domain.usecase.ImageDownloadUseCase
import com.mskd.flux.features.gallery.presentation.GalleryViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val moduleGallery = module {

    singleOf(::GetGalleryImagesUseCase)

    single<CachedImageDataSource> { CoilCachedImageDataSource(diskCache = get()) }
    single<RemoteImageDataSource> { KtorRemoteImageDataSource(client = get(named("uiClient"))) }
    single { ImageDownloadUseCase(cache = get(), remote = get(), saver = get()) }

    viewModel { params ->
        GalleryViewModel(
            artworkId = params.get(),
            contentType = params.get(),
            getGalleryImages = get(),
            databaseRepository = get(),
            connectivityRepository = get(),
            imageDownload = get(),
        )
    }

}
