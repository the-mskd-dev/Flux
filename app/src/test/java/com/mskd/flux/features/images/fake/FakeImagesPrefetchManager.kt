package com.mskd.flux.features.images.fake

import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeImagesPrefetchManager : ImagesPrefetchManager {

    override val state: Flow<ImagesPrefetchManager.State> =
        MutableStateFlow(ImagesPrefetchManager.State.Idle)

    override fun prefetchImages() {

    }
}