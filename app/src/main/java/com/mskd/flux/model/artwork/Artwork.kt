package com.mskd.flux.model.artwork

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mskd.flux.R
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBArtwork
import com.mskd.flux.model.tmdb.TMDBMovie

/**
 * Represents a media, such as a movie or a TV show.
 *
 * @property id Unique identifier for the media.
 * @property title Title of the media.
 * @property imagePath Path to the main image of the media.
 * @property bannerPath Path to the banner image of the media.
 * @property type Content of the media, which can be a movie or a show.
 */
@Entity(tableName = "artworks")
data class Artwork(
    @PrimaryKey
    val id: Long = 0,
    val title: String = "",
    val imagePath: String = "",
    val bannerPath: String = "",
    val type: ContentType = ContentType.SHOW
) {

    /**
     * Constructs an [Artwork] instance using a [TMDBMovie] and a [UserFile].
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
     * Constructs an [Artwork] instance using a [TMDBArtwork].
     */
    constructor(tmdbArtwork: TMDBArtwork) : this(
        id = tmdbArtwork.id,
        title = tmdbArtwork.title,
        imagePath = tmdbArtwork.imagePath,
        bannerPath = tmdbArtwork.bannerPath,
        type = ContentType.SHOW
    )

    val isUnknown: Boolean get() = id == UNKNOWN_ID

    companion object {

        const val UNKNOWN_ID = -616L

        val UNKNOWN = Artwork(
            id = UNKNOWN_ID,
            title = "Others",
            type = ContentType.SHOW
        )

    }

}

enum class ContentType {
    MOVIE,
    SHOW;

    val stringResource: Int get() = when (this) {
        MOVIE -> R.string.movies
        SHOW -> R.string.shows
    }

}