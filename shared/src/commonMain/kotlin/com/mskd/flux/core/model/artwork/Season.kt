package com.mskd.flux.core.model.artwork

/**
 * Represents a season of a show, grouping its episodes by season number.
 *
 * @property title Title of the season.
 * @property artworkId Identifier of the parent show.
 * @property season Season number.
 * @property imagePath Path to the season's image.
 * @property description Description or synopsis of the season.
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
