package com.kaem.flux.model.flux

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBCrew
import com.kaem.flux.model.tmdb.TMDBEpisode

/**
 * Represents an episode of a TV show.
 *
 * @property id Unique identifier for the episode.
 * @property title Title of the episode.
 * @property showId Identifier of the parent show.
 * @property number Episode number.
 * @property season Season number.
 * @property imagePath Path to the episode's image.
 * @property crew List of crew members involved in the episode.
 * @property releaseDateString The release date of the artwork as a string.
 * @property description Description or synopsis of the artwork.
 * @property voteAverage Average rating of the artwork.
 * @property voteCount Number of votes received for the artwork.
 * @property duration Duration of the artwork in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the artwork.
 * @property releaseDateString Release date of the episode as a string.
 */
@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = Artwork::class,
            parentColumns = ["id"],
            childColumns = ["artworkId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class Episode(
    @PrimaryKey
    val id: Long,
    val title: String,
    val number: Int,
    val season: Int,
    val imagePath: String,
    //val crew: List<TMDBCrew>,
    override val artworkId: Long,
    releaseDateString: String,
    description: String,
    duration: Int,
    currentTime: Long = 0L,
    voteAverage: Float,
    voteCount: Int,
    @Embedded override val file: UserFile,
    status: Status = Status.TO_WATCH,
) : ArtworkInfo(
    artworkId = artworkId,
    file = file,
    releaseDateString = releaseDateString,
    description = description,
    voteAverage = voteAverage,
    voteCount = voteCount,
    duration = duration,
    currentTime = currentTime,
    status = status
) {

    constructor(
        artworkId: Long,
        tmdbEpisode: TMDBEpisode,
        file: UserFile
    ) : this (
        id = tmdbEpisode.id,
        artworkId = artworkId,
        title = tmdbEpisode.title,
        number = tmdbEpisode.number,
        season = tmdbEpisode.season,
        imagePath = tmdbEpisode.imagePath,
        releaseDateString = tmdbEpisode.releaseDateString,
        //crew = tmdbEpisode.crew,
        description = tmdbEpisode.description,
        duration = tmdbEpisode.duration,
        currentTime = 0L,
        voteAverage = tmdbEpisode.voteAverage,
        voteCount = tmdbEpisode.voteCount,
        status = Status.TO_WATCH,
        file = file
    )

    companion object {

        /** Constant used when no ID is available. */
        const val NO_ID = -1

    }

}
