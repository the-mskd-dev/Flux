package com.kaem.flux.model.artwork

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBMovie
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Represents a movie with specific details.
 *
 * @property releaseDateString The release date of the media as a string.
 * @property description Description or synopsis of the media.
 * @property voteAverage Average rating of the media.
 * @property voteCount Number of votes received for the media.
 * @property duration Duration of the media in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the media.
 * @property genres List of genres associated with the movie.
 */
@Entity(
    tableName = "movies",
    foreignKeys = [
        ForeignKey(
            entity = Artwork::class,
            parentColumns = ["id"],
            childColumns = ["artworkId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Movie(
    @PrimaryKey
    override val artworkId: Long,
    override val title: String,
    override val releaseDateString: String,
    override val description: String,
    override val voteAverage: Float,
    override val voteCount: Int,
    override val duration: Int,
    override val currentTime: Long = 0L,
    override val status: Status = Status.TO_WATCH,
    @Embedded override val file: UserFile,
    //val genres: List<String> = listOf()
) : Media() {

    /**
     * Constructs a [Movie] instance using a [TMDBMovie] and a [UserFile].
     */
    constructor(
        tmdbMovie: TMDBMovie,
        file: UserFile
    ) : this(
        artworkId = tmdbMovie.id,
        title = tmdbMovie.title,
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