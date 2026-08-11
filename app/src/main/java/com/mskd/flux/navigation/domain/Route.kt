package com.mskd.flux.navigation.domain

import androidx.navigation3.runtime.NavKey
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Genre
import kotlinx.serialization.Serializable

sealed class Route : NavKey {

    @Serializable
    data object Setup: Route()

    @Serializable
    data class Token(val fromSetup: Boolean): Route()

    @Serializable
    data object Catalog: Route()

    @Serializable
    data class Show(val artworkId: Long, val rgb: Int?): Route()

    @Serializable
    data class Artwork(val artworkId: Long, val season: Int? = null, val rgb: Int?): Route()

    @Serializable
    data object UnknownArtworks: Route()

    @Serializable
    data class Search(
        val withType: ContentType? = null,
        val withGenre: Genre? = null,
    ): Route()

    @Serializable
    data class Player(val mediaId: Long) : Route()

    @Serializable
    data object Settings: Route()

    @Serializable
    data object HowTo: Route()

    @Serializable
    data object About: Route()

    @Serializable
    data object Customization: Route()

    @Serializable
    data class Sources(val fromSetup: Boolean = false): Route()
}