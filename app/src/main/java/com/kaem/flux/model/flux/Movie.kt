package com.kaem.flux.model.flux

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
class Movie(
    releaseDateString: String,
    description: String,
    voteAverage: Float,
    voteCount: Int,
    duration: Int,
    currentTime: Long = 0L,
    file: UserFile,
    status: Status,
    val genres: List<String> = listOf()
) : ArtworkInfo(
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
        releaseDateString = tmdbMovie.releaseDateString,
        description = tmdbMovie.description,
        voteAverage = tmdbMovie.voteAverage,
        voteCount = tmdbMovie.voteCount,
        duration = tmdbMovie.duration,
        currentTime = 0L,
        file = file,
        genres = emptyList(),
        status = Status.TO_WATCH
    )

}