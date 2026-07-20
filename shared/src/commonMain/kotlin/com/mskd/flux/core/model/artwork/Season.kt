package com.mskd.flux.core.model.artwork

/**
 * Represents an episode of a TV show.
 *
 * @property title Title of the episode.
 * @property artworkId Identifier of the parent show.
 * @property season Season number.
 * @property imagePath Path to the episode's image.
 * @property description Description or synopsis of the media.
 */
data class Season(
    val id: Long,
    val artworkId: Long,
    val title: String,
    val description: String,
    val imagePath: String?,
    val season: Int
) {

    val infoUrl: String get() = "https://www.themoviedb.org/tv/$artworkId/season/$season"

}
