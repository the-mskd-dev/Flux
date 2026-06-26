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

class ImagesUCImpl(
    private val database: DatabaseRepository,
    private val settings: SettingsRepository,
    private val imageLoader: ImageLoader,
    private val imageRequestFactory: ImageRequestFactory,
    private val scope: CoroutineScope,
) : ImagesUC {

    //region Variables

    private var _state = MutableStateFlow<ImagesUC.State>(ImagesUC.State.Idle)

    private val pendingUrls = Collections.synchronizedSet(mutableSetOf<String>())
    private val totalCount = AtomicInteger(0)
    private val completedCount = AtomicInteger(0)

    private val semaphore = Semaphore(2)

    //endregion

    //region Public methods

    override val state: Flow<ImagesUC.State> = _state.asStateFlow()

    override fun prefetchImages() {

        scope.launch {

            val prefetchHdImages = settings.flow.first().prefetchHdImages

            val allImagesPaths = database.getAllImagesPaths()

            val sdUrls = allImagesPaths.map { it.tmdbImage }.filter { pendingUrls.add(it) }
            val hdUrls = if (prefetchHdImages) allImagesPaths.map { it.tmdbImageLarge }.filter { pendingUrls.add(it) } else emptyList()
            val urls = (sdUrls + hdUrls).ifEmpty { return@launch }

            totalCount.addAndGet(urls.size)
            updateState()

            val onFetchEnd: (String) -> Unit = { url ->
                pendingUrls.remove(url)
                completedCount.incrementAndGet()
                updateState()
                semaphore.release()
            }

            urls.forEach { url ->
                launch {

                    semaphore.acquire()

                    val request = imageRequestFactory.build(
                        url = url,
                        onEnd = { onFetchEnd(url) }
                    )

                    imageLoader.enqueue(request)

                }
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