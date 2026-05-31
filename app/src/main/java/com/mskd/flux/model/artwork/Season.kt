package com.mskd.flux.model.artwork

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mskd.flux.model.tmdb.TMDBSeason

/**
 * Represents an episode of a TV show.
 *
 * @property title Title of the episode.
 * @property artworkId Identifier of the parent show.
 * @property season Season number.
 * @property imagePath Path to the episode's image.
 * @property description Description or synopsis of the media.
 */
@Entity(
    tableName = "seasons",
    indices = [
        Index(value = ["artworkId"])
    ]
)
data class Season(
    @PrimaryKey
    val id: Long,
    val artworkId: Long,
    val title: String,
    val description: String,
    val imagePath: String?,
    val season: Int
) {

    constructor(
        tmdbSeason: TMDBSeason,
        artworkId: Long
    ) : this(
        id = tmdbSeason.id,
        artworkId = artworkId,
        title = tmdbSeason.title,
        description = tmdbSeason.description,
        imagePath = tmdbSeason.imagePath.orEmpty(),
        season = tmdbSeason.season
    )

    val infoUrl: String get() = "https://www.themoviedb.org/tv/$artworkId/season/$season"

}
