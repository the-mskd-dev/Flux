package com.mskd.flux.features.images.fake

import com.mskd.flux.features.images.domain.usecase.PrefetchImagesUseCase

class FakePrefetchImagesUseCase : PrefetchImagesUseCase {

    var prefetchCount = 0
        private set

    override operator fun invoke() {
        prefetchCount++
    }
}
