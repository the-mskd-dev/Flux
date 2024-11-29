package com.kaem.flux.model.flux

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMovie

data class Artwork(
    val id: Int = -1,
    val title: String = "",
    val imagePath: String = "",
    val bannerPath: String = "",
    val content: Content = Content.SHOW()
) {

    val description: String? = when (content) {
        is Content.MOVIE -> content.movie.description
        is Content.SHOW -> content.currentEpisode?.description
    }

    constructor(
        tmdbMovie: TMDBMovie,
        file: UserFile
    ) : this (
        id = tmdbMovie.id,
        title = tmdbMovie.title,
        imagePath = tmdbMovie.imagePath,
        bannerPath = tmdbMovie.bannerPath,
        content = Content.MOVIE(movie = Movie(tmdbMovie = tmdbMovie, file = file))
    )

    constructor(tmdbArtwork: TMDBArtwork) : this(
        id = tmdbArtwork.id,
        title = tmdbArtwork.title,
        imagePath = tmdbArtwork.imagePath,
        bannerPath = tmdbArtwork.bannerPath,
        content = Content.SHOW()
    )

}