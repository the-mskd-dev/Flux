package com.mskd.flux.features.images.data.usecase

import coil3.ImageLoader
import coil3.size.Size
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.images.domain.ImageRequestFactory
import com.mskd.flux.features.images.domain.LOGO_REQUEST_SIZE
import com.mskd.flux.features.images.domain.usecase.PrefetchImagesUseCase
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.tmdbImageLarge
import com.mskd.flux.utils.extensions.tmdbLogo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore

class PrefetchImagesUseCaseImpl(
    private val database: DatabaseRepository,
    private val imageLoader: ImageLoader,
    private val imageRequestFactory: ImageRequestFactory,
    private val scope: CoroutineScope,
) : PrefetchImagesUseCase {

    //region Variables

    private val pendingUrls = MutableStateFlow<Set<String>>(emptySet())

    private val semaphore = Semaphore(2)

    //endregion

    //region Public methods

    override operator fun invoke() {

        scope.launch {

            val imagesPaths = database.getAllImagesPaths()

            val sdUrls = imagesPaths.images.map { it.tmdbImage }.let(::reservePendingUrls)
            val hdUrls = imagesPaths.images.map { it.tmdbImageLarge }.let(::reservePendingUrls)
            // A Logo is requested like on its Artwork page to share its cache entry, never in HD
            val logoUrls = imagesPaths.logos.mapNotNull { path -> path?.takeIf { it.isNotEmpty() }?.tmdbLogo }.let(::reservePendingUrls)
            val sizeByUrl: Map<String, Size?> = (sdUrls + hdUrls).associateWith { null } + logoUrls.associateWith { LOGO_REQUEST_SIZE }
            val urls = sizeByUrl.keys.toList().ifEmpty { return@launch }

            val onFetchEnd: (String) -> Unit = { url ->
                pendingUrls.update { it - url }
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

    //endregion

}
