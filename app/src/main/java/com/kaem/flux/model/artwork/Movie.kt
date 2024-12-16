package com.kaem.flux.model.artwork

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBMovie

/**
 * Represents a movie with specific details.
 *
 * @property releaseDateString The release date of the artwork as a string.
 * @property description Description or synopsis of the artwork.
 * @property voteAverage Average rating of the artwork.
 * @property voteCount Number of votes received for the artwork.
 * @property duration Duration of the artwork in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the artwork.
 * @property genres List of genres associated with the movie.
 */
@Entity(
    tableName = "movies",
    foreignKeys = [
        ForeignKey(
            entity = ArtworkOverview::class,
            parentColumns = ["id"],
            childColumns = ["artworkId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Movie(
    @PrimaryKey
    override val artworkId: Long,
    override val releaseDateString: String,
    override val description: String,
    override val voteAverage: Float,
    override val voteCount: Int,
    override val duration: Int,
    override var currentTime: Long = 0L,
    override var status: Status,
    @Embedded override val file: UserFile,
    //val genres: List<String> = listOf()
) : Artwork(
    artworkId = artworkId,
    releaseDateString = releaseDateString,
    description = description,
    voteAverage = voteAverage,
    voteCount = voteCount,
    duration = duration,
    currentTime = currentTime,
    file = file,
    status = status
) {

    /**
     * Constructs a [Movie] instance using a [TMDBMovie] and a [UserFile].
     */
    constructor(
        tmdbMovie: TMDBMovie,
        file: UserFile
    ) : this(
        artworkId = tmdbMovie.id,
        releaseDateString = tmdbMovie.releaseDateString,
        description = tmdbMovie.description,
        voteAverage = tmdbMovie.voteAverage,
        voteCount = tmdbMovie.voteCount,
        duration = tmdbMovie.duration,
        currentTime = 0L,
        file = file,
        //genres = emptyList(),
        status = Status.TO_WATCH
    )

}