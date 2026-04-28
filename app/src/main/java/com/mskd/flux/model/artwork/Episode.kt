package com.mskd.flux.model.artwork

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBEpisode
import kotlin.random.Random

/**
 * Represents an episode of a TV show.
 *
 * @property id Unique identifier for the episode.
 * @property title Title of the episode.
 * @property artworkId Identifier of the parent show.
 * @property number Episode number.
 * @property season Season number.
 * @property imagePath Path to the episode's image.
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
    ],
    indices = [
        Index(value = ["artworkId"])
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

    constructor(file: UserFile, duration: Int = 0) : this (
        id = -Random.nextLong(),
        artworkId = Artwork.UNKNOWN_ID,
        title = file.nameProperties.title,
        number = file.nameProperties.episode ?: -1,
        season = file.nameProperties.season ?: -1,
        imagePath = "",
        releaseDateString = "",
        description = "",
        duration = duration,
        currentTime = 0L,
        voteAverage = 0f,
        voteCount = 0,
        status = Status.TO_WATCH,
        file = file
    )

    val isUnknown: Boolean get() = artworkId == Artwork.UNKNOWN_ID

    val infoUrl: String get() = "https://www.themoviedb.org/tv/$artworkId/season/$season/episode/$number"

}
