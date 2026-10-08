package com.mskd.flux.navigation.domain

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.features.player.domain.model.PlayerParams
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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
    data class Player(val params: PlayerParams) : Route()

    @Serializable
    data object Settings: Route()

    @Serializable
    data object PrivateFolder: Route()

    @Serializable
    data object HowTo: Route()

    @Serializable
    data object About: Route()

    @Serializable
    data object Customization: Route()

    @Serializable
    data class Sources(val fromSetup: Boolean = false): Route()

    @Serializable
    data object Message: Route()

}

val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Setup::class)
            subclass(Route.Catalog::class)
            subclass(Route.Show::class)
            subclass(Route.Artwork::class)
            subclass(Route.UnknownArtworks::class)
            subclass(Route.PrivateFolder::class)
            subclass(Route.Search::class)
            subclass(Route.Player::class)
            subclass(Route.Settings::class)
            subclass(Route.Customization::class)
            subclass(Route.HowTo::class)
            subclass(Route.About::class)
            subclass(Route.Token::class)
            subclass(Route.Sources::class)
            subclass(Route.Message::class)
        }
    }
}

fun Route?.isSameTabAs(target: Route): Boolean = when (target) {
    is Route.Catalog -> this is Route.Catalog
    is Route.Search -> this is Route.Search
    is Route.Settings -> this is Route.Settings
    else -> false
}