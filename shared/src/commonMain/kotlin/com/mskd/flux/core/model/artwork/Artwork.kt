package com.mskd.flux.core.model.artwork

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Represents an artwork (a movie or a show) as identified on TMDB, without its media.
 *
 * @property id Unique TMDB identifier of the artwork.
 * @property title Title of the artwork.
 * @property imagePath Path to the main image of the artwork.
 * @property bannerPath Path to the banner image of the artwork.
 * @property type Content type of the artwork, which can be a movie or a show.
 */
data class Artwork(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val imagePath: String = "",
    val bannerPath: String = "",
    val type: ContentType = ContentType.SHOW,
    val genreIds: ImmutableList<Int> = persistentListOf(),
    val isPrivate: Boolean = false,
    val nsfw: Boolean = false,

    val lastModification: Long? = null,
) {

    val isUnknown: Boolean get() = id == UNKNOWN_ID

    val infoUrl: String get() = when (type) {
        ContentType.MOVIE -> "https://www.themoviedb.org/movie/$id"
        ContentType.SHOW -> "https://www.themoviedb.org/tv/$id"
    }

    companion object {

        const val UNKNOWN_ID = -616L

        val UNKNOWN = Artwork(
            id = UNKNOWN_ID,
            title = "Others",
            type = ContentType.SHOW
        )

    }

}