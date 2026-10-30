package com.mskd.flux.features.images.domain

import coil3.ImageLoader
import coil3.size.Size
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.tmdbImageLarge
import com.mskd.flux.utils.extensions.tmdbLogo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
class ImagesPrefetchManagerImpl(
    private val database: DatabaseRepository,
    private val settings: SettingsDataStore,
    private val imageLoader: ImageLoader,
    private val imageRequestFactory: ImageRequestFactory,
    private val scope: CoroutineScope,
) : ImagesPrefetchManager {

    //region Variables

    private var _state =
        MutableStateFlow<ImagesPrefetchManager.State>(ImagesPrefetchManager.State.Idle)

    private val pendingUrls = MutableStateFlow<Set<String>>(emptySet())

    private val totalCount = AtomicInt(0)
    private val completedCount = AtomicInt(0)

    private val semaphore = Semaphore(2)

    //endregion

    //region Public methods

    override val state: Flow<ImagesPrefetchManager.State> = _state.asStateFlow()

    override fun prefetchImages() {

        scope.launch {

            val prefetchHdImages = settings.flow.first().prefetchHdImages

            val imagesPaths = database.getAllImagesPaths()

            val sdUrls = imagesPaths.images.map { it.tmdbImage }.let(::reservePendingUrls)
            val hdUrls = if (prefetchHdImages) imagesPaths.images.map { it.tmdbImageLarge }.let(::reservePendingUrls) else emptyList()
            // A Logo is requested like on its Artwork page to share its cache entry, never in HD
            val logoUrls = imagesPaths.logos.mapNotNull { path -> path?.takeIf { it.isNotEmpty() }?.tmdbLogo }.let(::reservePendingUrls)
            val sizeByUrl: Map<String, Size?> = (sdUrls + hdUrls).associateWith { null } + logoUrls.associateWith { LOGO_REQUEST_SIZE }
            val urls = sizeByUrl.keys.toList().ifEmpty { return@launch }

            totalCount.addAndFetch(urls.size)
            updateState()

            val onFetchEnd: (String) -> Unit = { url ->
                pendingUrls.update { it - url }
                completedCount.incrementAndFetch()
                updateState()
                semaphore.release()
            }

            urls.forEach { url ->
                launch {

                    semaphore.acquire()

                    val request = imageRequestFactory.build(
                        url = url,
                        onEnd = { onFetchEnd(url) },
                        size = sizeByUrl[url]
                    )

                    imageLoader.enqueue(request)

                }
            }

        }

    }

    //endregion

    //region Private methods

    /** Atomically adds the [urls] not already pending and returns them */
    private fun reservePendingUrls(urls: List<String>): List<String> {
        var reserved = emptyList<String>()
        pendingUrls.update { pending ->
            reserved = urls.distinct().filterNot { it in pending }
            pending + reserved
        }
        return reserved
    }

    private fun updateState() {

        val total = totalCount.load()
        val completed = completedCount.load()

        if (completed >= total) {
            totalCount.store(0)
            completedCount.store(0)
            _state.value = ImagesPrefetchManager.State.Idle
        } else {
            _state.value = ImagesPrefetchManager.State.InProgress(completed.toFloat() / total)
        }
    }

    //endregion

}