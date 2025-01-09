package com.kaem.flux.model.artwork

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kaem.flux.R
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBOverview
import com.kaem.flux.model.tmdb.TMDBMovie

/**
 * Represents an artwork, such as a movie or a TV show.
 *
 * @property id Unique identifier for the artwork.
 * @property title Title of the artwork.
 * @property imagePath Path to the main image of the artwork.
 * @property bannerPath Path to the banner image of the artwork.
 * @property type Content of the artwork, which can be a movie or a show.
 * @property description Short description derived from the associated content.
 */
@Entity(tableName = "artworks")
data class ArtworkOverview(
    @PrimaryKey
    val id: Long = 0,
    val title: String = "",
    val imagePath: String = "",
    val bannerPath: String = "",
    val type: ContentType = ContentType.SHOW
) {

    /**
     * Constructs an [ArtworkOverview] instance using a [TMDBMovie] and a [UserFile].
     */
    constructor(
        tmdbMovie: TMDBMovie,
    ) : this (
        id = tmdbMovie.id,
        title = tmdbMovie.title,
        imagePath = tmdbMovie.imagePath,
        bannerPath = tmdbMovie.bannerPath,
        type = ContentType.MOVIE
    )

    /**
     * Constructs an [ArtworkOverview] instance using a [TMDBOverview].
     */
    constructor(tmdbOverview: TMDBOverview) : this(
        id = tmdbOverview.id,
        title = tmdbOverview.title,
        imagePath = tmdbOverview.imagePath,
        bannerPath = tmdbOverview.bannerPath,
        type = ContentType.SHOW
    )

}

enum class ContentType {
    MOVIE,
    SHOW;

    val stringResource: Int get() = when (this) {
        MOVIE -> R.string.movies
        SHOW -> R.string.shows
    }

}