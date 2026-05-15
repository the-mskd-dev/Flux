package com.mskd.flux.useCases.images

import android.content.Context
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

class ImagesUCImpl(
    private val database: DatabaseRepository,
    private val imageLoader: ImageLoader,
    private val context: Context,
    private val scope: CoroutineScope,
) : ImagesUC {

    //region Variables

    private var _state = MutableStateFlow<ImagesUC.State>(ImagesUC.State.Idle)

    private val pendingUrls = Collections.synchronizedSet(mutableSetOf<String>())
    private val totalCount = AtomicInteger(0)
    private val completedCount = AtomicInteger(0)

    //endregion

    //region Public methods

    override val state: Flow<ImagesUC.State> = _state.asStateFlow()

    override fun prefetchImages() {

        scope.launch {

            val urls = database
                .getAllImagesPaths()
                .filter { pendingUrls.add(it) }
                .ifEmpty { return@launch }

            totalCount.addAndGet(urls.size)
            updateState()

            urls.forEach { url ->

                val onFetchEnd: () -> Unit = {
                    pendingUrls.remove(url)
                    completedCount.incrementAndGet()
                    updateState()
                }

                val request = ImageRequest.Builder(context)
                    .data(url)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .listener(
                        onSuccess = { _, _ -> onFetchEnd() },
                        onError = { _, _ -> onFetchEnd() },
                        onCancel = { _ -> onFetchEnd() }
                    )
                    .build()

                imageLoader.enqueue(request)

            }

        }

    }

    //endregion

    //region Private methods

    private fun updateState() {

        val total = totalCount.get()
        val completed = completedCount.get()

        if (completed >= total) {
            totalCount.set(0)
            completedCount.set(0)
            _state.value = ImagesUC.State.Idle
        } else {
            _state.value = ImagesUC.State.InProgress(completed.toFloat() / total)
        }
    }

    //endregion

}