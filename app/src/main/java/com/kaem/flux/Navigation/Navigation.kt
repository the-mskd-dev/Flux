package com.kaem.flux.Navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

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