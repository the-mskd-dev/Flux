package com.kaem.flux.model.flux

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBMovie

class Movie(
    releaseDateString: String,
    description: String,
    voteAverage: Float,
    voteCount: Int,
    duration: Int,
    currentTime: Long = 0L,
    file: UserFile,
    status: FluxStatus,
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
        status = FluxStatus.TO_WATCH
    )

}