package com.kaem.flux.model.media

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kaem.flux.R
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBMovie
import com.kaem.flux.model.tmdb.TMDBOverview

/**
 * Represents a media, such as a movie or a TV show.
 *
 * @property id Unique identifier for the media.
 * @property title Title of the media.
 * @property imagePath Path to the main image of the media.
 * @property bannerPath Path to the banner image of the media.
 * @property type Content of the media, which can be a movie or a show.
 */
@Entity(tableName = "medias")
data class MediaOverview(
    @PrimaryKey
    val id: Long = 0,
    val title: String = "",
    val imagePath: String = "",
    val bannerPath: String = "",
    val type: ContentType = ContentType.SHOW
) {

    /**
     * Constructs an [MediaOverview] instance using a [TMDBMovie] and a [UserFile].
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
     * Constructs an [MediaOverview] instance using a [TMDBOverview].
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