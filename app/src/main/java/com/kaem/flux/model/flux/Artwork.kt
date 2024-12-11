package com.kaem.flux.model.flux

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMovie

enum class ContentType { MOVIE, SHOW }

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
data class Artwork(
    @PrimaryKey
    val id: Long = 0,
    val title: String = "",
    val imagePath: String = "",
    val bannerPath: String = "",
    val type: ContentType = ContentType.SHOW
) {

    /*val description: String? = when (type) {
        is ArtworkType.MOVIE -> type.movie.description
        is ArtworkType.SHOW -> type.currentEpisode?.description
    }*/


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

}