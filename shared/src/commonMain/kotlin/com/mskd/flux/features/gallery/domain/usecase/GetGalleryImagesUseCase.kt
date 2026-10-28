package com.mskd.flux.features.gallery.domain.usecase

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.gallery.domain.model.GalleryImage

/**
 * Lists the images of the Gallery of an Artwork: backdrops and posters mixed, best rated first.
 */
class GetGalleryImagesUseCase(
    private val apiRepository: ApiRepository,
) {

    suspend operator fun invoke(artworkId: Long, contentType: ContentType): List<GalleryImage> {

        val images = when (contentType) {
            ContentType.MOVIE -> apiRepository.getMovieImages(artworkId = artworkId)
            ContentType.SHOW -> apiRepository.getShowImages(artworkId = artworkId)
        }

        return images.sortedByDescending { it.rating }

    }

}
