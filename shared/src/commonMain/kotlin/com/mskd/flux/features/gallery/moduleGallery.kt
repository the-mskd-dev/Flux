package com.mskd.flux.features.gallery

import com.mskd.flux.features.gallery.domain.usecase.GetGalleryImagesUseCase
import com.mskd.flux.features.gallery.presentation.GalleryViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleGallery = module {

    singleOf(::GetGalleryImagesUseCase)

    viewModel { params ->
        GalleryViewModel(
            artworkId = params.get(),
            contentType = params.get(),
            getGalleryImages = get(),
            databaseRepository = get(),
            connectivityRepository = get(),
        )
    }

}
