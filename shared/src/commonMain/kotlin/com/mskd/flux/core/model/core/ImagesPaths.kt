package com.mskd.flux.core.model.core

/**
 * TMDB image paths of the Catalogue to prefetch.
 * Logos are kept separate since they are prefetched in a single size, unlike the other images.
 * A Logo path is null or empty when the Artwork has no Logo.
 */
data class ImagesPaths(
    val images: List<String> = emptyList(),
    val logos: List<String?> = emptyList(),
)
