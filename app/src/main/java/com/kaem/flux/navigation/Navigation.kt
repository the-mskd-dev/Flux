package com.kaem.flux.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    data object Library: Route()
    data class Category(val category: String): Route()
    data class Media(val id: Long): Route()
    data object Search: Route()
    data object Settings: Route()
    data object HowTo: Route()
    data object About: Route()
}

sealed class Navigation(protected val base: String) {

    open val route: String = base
    open val arguments: List<NamedNavArgument> = emptyList()

    fun build(args: List<Any> = emptyList()) : String {
        if (args.isEmpty()) return route
        return base +  args.joinToString(separator = "/", prefix = "/")
    }

    object LIBRARY : Navigation("library")

    object CATEGORY : Navigation("category") {

        override val route: String = "$base/{contentType}"
        override val arguments: List<NamedNavArgument> = listOf(navArgument("contentType") { type = NavType.StringType })

    }

    object MEDIA : Navigation("media") {
        override val route: String = "$base/{mediaId}"
        override val arguments: List<NamedNavArgument> = listOf(navArgument("mediaId") { type = NavType.LongType })

    }

    object SEARCH : Navigation("search")
    object SETTINGS : Navigation("settings")
    object HOW_TO : Navigation("howTo")
    object ABOUT : Navigation("about")
}