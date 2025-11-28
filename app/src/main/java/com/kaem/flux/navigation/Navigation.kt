package com.kaem.flux.navigation

import androidx.navigation3.runtime.NavKey
import com.kaem.flux.model.media.ContentType
import kotlinx.serialization.Serializable

sealed class Route : NavKey {

    @Serializable
    data object Library: Route()

    @Serializable
    data class Category(val contentType: ContentType): Route()

    @Serializable
    data class Media(val mediaId: Long): Route()

    @Serializable
    data object Search: Route()

    @Serializable
    data object Settings: Route()

    @Serializable
    data object HowTo: Route()

    @Serializable
    data object About: Route()
}