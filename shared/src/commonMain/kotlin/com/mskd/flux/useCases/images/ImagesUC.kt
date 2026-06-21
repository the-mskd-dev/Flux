package com.mskd.flux.useCases.images

import coil3.ImageLoader
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.platform.ImageRequestFactory
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.tmdbImageLarge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

interface ImagesUC {

    val state: Flow<State>
    fun prefetchImages()

    sealed class State {
        data object Idle : State()
        data class InProgress(val progress: Float) : State()
    }

}