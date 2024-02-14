package com.kaem.flux.model.flux

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBMovie

class FluxMovie(
    id: Int,
    title: String,
    imagePath: String,
    bannerPath: String,
    releaseDateString: String,
    override val description: String,
    override val voteAverage: Float,
    override val voteCount: Int,
    override val duration: Int,
    override val file: UserFile,
    override var status: FluxStatus = FluxStatus.NOT_WATCHED,
    val genres: List<String> = listOf(),
) : FluxArtwork(
    id = id,
    title = title,
    imagePath = imagePath,
    bannerPath = bannerPath,
    releaseDateString = releaseDateString
), FluxArtworkDetails {

    constructor(
        tmdbMovie: TMDBMovie,
        file: UserFile
    ) : this(
        id = tmdbMovie.id,
        title = tmdbMovie.title,
        imagePath = tmdbMovie.imagePath,
        bannerPath = tmdbMovie.bannerPath,
        releaseDateString = tmdbMovie.releaseDateString,
        description = tmdbMovie.description,
        voteAverage = tmdbMovie.voteAverage,
        voteCount = tmdbMovie.voteCount,
        duration = tmdbMovie.duration,
        file = file,
        genres = emptyList()
    )
}
