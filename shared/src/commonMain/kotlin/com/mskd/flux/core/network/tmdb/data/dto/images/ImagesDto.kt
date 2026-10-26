package com.mskd.flux.core.network.tmdb.data.dto.images

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Images of a movie or TV show retrieved from TMDB. Logos are not read.
 *
 * @property backdrops Horizontal images.
 * @property posters Vertical images.
 */
@Serializable
data class ImagesDto(
    val backdrops: List<ImageDto> = emptyList(),
    val posters: List<ImageDto> = emptyList(),
)

/**
 * A single TMDB image.
 *
 * @property filePath Path of the image, to append to the TMDB image base url.
 * @property aspectRatio Width divided by height.
 * @property voteAverage Rating of the image.
 * @property language ISO 639-1 language of the image, null when the image has no text.
 */
@Serializable
data class ImageDto(
    @SerialName("file_path")
    val filePath: String,
    @SerialName("aspect_ratio")
    val aspectRatio: Double,
    @SerialName("vote_average")
    val voteAverage: Double = 0.0,
    @SerialName("iso_639_1")
    val language: String? = null,
)
