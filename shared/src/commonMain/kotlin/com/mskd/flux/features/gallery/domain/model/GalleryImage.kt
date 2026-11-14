package com.mskd.flux.features.gallery.domain.model

import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.tmdbImageLarge

/**
 * An image of the Gallery of an Artwork.
 *
 * @property path TMDB path of the image.
 * @property type Backdrop, poster or logo.
 * @property ratio Width divided by height.
 * @property rating TMDB rating of the image.
 */
data class GalleryImage(
    val path: String,
    val type: GalleryImageType,
    val ratio: Float,
    val rating: Double,
)

val GalleryImage.thumbnailUrl: String
    get() = when (type) {
        GalleryImageType.POSTER -> Constants.TMDB.IMAGE_GALLERY_POSTER + path
        GalleryImageType.BACKDROP -> Constants.TMDB.IMAGE_GALLERY_BACKDROP + path
        GalleryImageType.LOGO -> Constants.TMDB.IMAGE_LOGO + path
    }

val GalleryImage.originalUrl: String
    get() = path.tmdbImageLarge

enum class GalleryImageType {
    BACKDROP,
    POSTER,
    LOGO
}
