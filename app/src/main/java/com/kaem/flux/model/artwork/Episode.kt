package com.kaem.flux.model.artwork

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kaem.flux.model.UserFile
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
 * @property releaseDateString The release date of the media as a string.
 * @property description Description or synopsis of the media.
 * @property voteAverage Average rating of the media.
 * @property voteCount Number of votes received for the media.
 * @property duration Duration of the media in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the media.
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
data class Episode(
    @PrimaryKey
    val id: Long,
    val number: Int,
    val season: Int,
    val imagePath: String,
    //val crew: List<TMDBCrew>,
    override val artworkId: Long,
    override val title: String,
    override val releaseDateString: String,
    override val description: String,
    override val duration: Int,
    override val currentTime: Long = 0L,
    override val voteAverage: Float,
    override val voteCount: Int,
    @Embedded override val file: UserFile,
    override val status: Status = Status.TO_WATCH,
) : Media() {

    override val mediaId: Long get() = id

    constructor(
        mediaId: Long,
        tmdbEpisode: TMDBEpisode,
        file: UserFile
    ) : this (
        id = tmdbEpisode.id,
        artworkId = mediaId,
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

}
