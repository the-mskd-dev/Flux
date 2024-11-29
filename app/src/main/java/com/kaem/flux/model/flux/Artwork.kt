package com.kaem.flux.model.flux

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMovie

/**
 * Represents an artwork, such as a movie or a TV show.
 *
 * @property id Unique identifier for the artwork.
 * @property title Title of the artwork.
 * @property imagePath Path to the main image of the artwork.
 * @property bannerPath Path to the banner image of the artwork.
 * @property content Content of the artwork, which can be a movie or a show.
 * @property description Short description derived from the associated content.
 */
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


    /**
     * Constructs an [Artwork] instance using a [TMDBMovie] and a [UserFile].
     */
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

    /**
     * Constructs an [Artwork] instance using a [TMDBArtwork].
     */
    constructor(tmdbArtwork: TMDBArtwork) : this(
        id = tmdbArtwork.id,
        title = tmdbArtwork.title,
        imagePath = tmdbArtwork.imagePath,
        bannerPath = tmdbArtwork.bannerPath,
        content = Content.SHOW()
    )

}