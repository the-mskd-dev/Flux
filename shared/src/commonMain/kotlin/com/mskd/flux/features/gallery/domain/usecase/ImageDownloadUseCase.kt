package com.mskd.flux.features.gallery.domain.usecase

import com.mskd.flux.features.gallery.domain.datasource.CachedImageDataSource
import com.mskd.flux.features.gallery.domain.datasource.RemoteImageDataSource
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.originalUrl
import com.mskd.flux.system.ImageSaver

/**
 * Image Download: copies the original resolution file of a Gallery image into the image storage of the device.
 * The bytes come from the cache of the images already displayed, and from the network when they are not in it.
 */
class ImageDownloadUseCase(
    private val cache: CachedImageDataSource,
    private val remote: RemoteImageDataSource,
    private val saver: ImageSaver,
) {

    /**
     * @throws Exception if the image is available neither in the cache nor from the network, or cannot be saved.
     */
    suspend operator fun invoke(image: GalleryImage, artworkTitle: String) {
        val bytes = cache.get(image.originalUrl) ?: remote.fetch(image.originalUrl)
        val extension = image.path.substringAfterLast('.', missingDelimiterValue = DEFAULT_EXTENSION).lowercase()
        saver.save(
            bytes = bytes,
            fileName = fileName(image = image, artworkTitle = artworkTitle, extension = extension),
            mimeType = mimeType(extension),
        ).getOrThrow()
    }

    // <Artwork title>_<basename of the TMDB path>.<extension>
    private fun fileName(image: GalleryImage, artworkTitle: String, extension: String): String {
        val basename = image.path.substringAfterLast('/').substringBeforeLast('.')
        val title = artworkTitle.replace(FORBIDDEN_CHARACTERS, "_").take(MAX_TITLE_LENGTH).trim()
        return if (title.isEmpty()) "$basename.$extension" else "${title}_$basename.$extension"
    }

    private fun mimeType(extension: String): String = when (extension) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "image/jpeg"
    }

    private companion object {
        const val DEFAULT_EXTENSION = "jpg"

        // Keeps the whole name under the 255 bytes of the file systems, whatever the script of the title
        const val MAX_TITLE_LENGTH = 60

        // Reserved by Android and desktop file systems, plus control characters
        val FORBIDDEN_CHARACTERS = Regex("""[\\/:*?"<>|\p{Cc}]""")
    }

}
