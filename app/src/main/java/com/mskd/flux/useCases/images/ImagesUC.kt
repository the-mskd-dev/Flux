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
import java.util.Collections
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

    private val activeUrls = Collections.synchronizedSet(mutableSetOf<String>())
    private val totalToProcess = AtomicInteger(0)
    private val completedCount = AtomicInteger(0)

    //endregion

    //region Public methods

    override val state: Flow<ImagesUC.State> = _state.asStateFlow()

    override fun prefetchImages() {

        scope.launch {

            val urls = database
                .getAllImagesPaths()
                .filter { activeUrls.add(it) }
                .ifEmpty { return@launch }

            if (urls.isEmpty()) return@launch

            val currentTotal = totalToProcess.addAndGet(urls.size)
            _state.value = ImagesUC.State.InProgress(completedCount.get().toFloat() / currentTotal)

            urls.forEach { url ->

                launch(Dispatchers.IO) {

                    try {

                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()

                        imageLoader.execute(request)

                    } finally {

                        activeUrls.remove(url)
                        val completed = completedCount.incrementAndGet()
                        val total = totalToProcess.get()

                        updateState(completed, total)

                    }

                }

            }

        }

    }

    //endregion

    //region Private methods

    private fun updateState(completed: Int, total: Int) {
        if (completed >= total) {
            totalToProcess.set(0)
            completedCount.set(0)
            _state.value = ImagesUC.State.Idle
        } else {
            _state.value = ImagesUC.State.InProgress(completed.toFloat() / total)
        }
    }

    //endregion

}