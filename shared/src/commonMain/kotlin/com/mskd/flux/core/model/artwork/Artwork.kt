package com.mskd.flux.core.model.artwork

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Represents a media, such as a movie or a TV show.
 *
 * @property id Unique identifier for the media.
 * @property title Title of the media.
 * @property imagePath Path to the main image of the media.
 * @property bannerPath Path to the banner image of the media.
 * @property type Content of the media, which can be a movie or a show.
 */
data class Artwork(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val imagePath: String = "",
    val bannerPath: String = "",
    val type: ContentType = ContentType.SHOW,
    val genreIds: ImmutableList<Int> = persistentListOf(),

    val lastModification: Long? = null
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