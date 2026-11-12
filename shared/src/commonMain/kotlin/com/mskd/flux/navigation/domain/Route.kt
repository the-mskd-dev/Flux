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

    /** Season page: the Season [season] of the Show [artworkId]. */
    @Serializable
    data class Season(val artworkId: Long, val season: Int, val rgb: Int?): Route()

    @Serializable
    data class Person(val person: com.mskd.flux.core.model.artwork.Person) : Route()

    /** Movie page of the Artwork [artworkId], a film. */
    @Serializable
    data class Movie(val artworkId: Long, val rgb: Int?): Route()

    @Serializable
    data class Gallery(val artworkId: Long, val contentType: ContentType): Route()

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

    /**
     * A page saved by a previous version that no longer exists (as the Artwork route, replaced by [Movie] and
     * [Season]): it is not restored, and leaves the back stack as soon as it is shown.
     */
    @Serializable
    data object Unrestorable: Route()

}

val routesSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(Route.Setup::class)
        subclass(Route.Catalog::class)
        subclass(Route.Show::class)
        subclass(Route.Season::class)
        subclass(Route.Person::class)
        subclass(Route.Movie::class)
        subclass(Route.Gallery::class)
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
        subclass(Route.Unrestorable::class)
        // A saved back stack may hold a page this version no longer knows: it must not crash the app.
        defaultDeserializer { Route.Unrestorable.serializer() }
    }
}

val navConfig = SavedStateConfiguration {
    serializersModule = routesSerializersModule
}

fun Route?.isSameTabAs(target: Route): Boolean = when (target) {
    is Route.Catalog -> this is Route.Catalog
    is Route.Search -> this is Route.Search
    is Route.Settings -> this is Route.Settings
    else -> false
}