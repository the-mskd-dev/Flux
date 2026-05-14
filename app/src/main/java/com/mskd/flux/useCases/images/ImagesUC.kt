package com.mskd.flux.useCases.images

import android.content.Context
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.useCases.catalog.CatalogUC
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

interface ImagesUC {

    val state: Flow<State>
    fun prefetchImages()

    sealed class State {
        data object Idle : State()
        data class InProgress(val progress: Float) : State()
    }

}

class ImagesUCImpl(
    private val database: DatabaseRepository,
    private val imageLoader: ImageLoader,
    private val context: Context,
    private val scope: CoroutineScope,
) : ImagesUC {

    //region Variables

    private var _state = MutableStateFlow<ImagesUC.State>(ImagesUC.State.Idle)

    private var fetchJob: Job? = null

    //endregion

    //region Public methods

    override val state: Flow<ImagesUC.State> = _state.asStateFlow()

    override fun prefetchImages() {

        fetchJob?.cancel()

        fetchJob = scope.launch {

            val urls = database
                .getAllImagesPaths()
                .ifEmpty { return@launch }

            val total = urls.size
            val completed = AtomicInteger(0)
            _state.value = ImagesUC.State.InProgress(0f)

            urls.forEach { url ->
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .listener(
                        onSuccess = { _, _ -> onCompleted(completed.incrementAndGet(), total) },
                        onError = { _, _ -> onCompleted(completed.incrementAndGet(), total) }
                    )
                    .build()
                imageLoader.enqueue(request)
            }

        }

    }

    //endregion

    //region Private methods

    private fun onCompleted(current: Int, total: Int) {
        _state.value = if (current >= total) {
            ImagesUC.State.Idle
        } else {
            ImagesUC.State.InProgress(current.toFloat() / total)
        }
    }

    //endregion

}