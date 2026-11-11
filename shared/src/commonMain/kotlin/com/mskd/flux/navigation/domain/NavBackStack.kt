package com.mskd.flux.navigation.domain

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.popScreen() {
    if (this.size > 1) this.removeLastOrNull()
}
/**
 * Replaces the current page, and the pages of the same Artwork [artworkId] right below it (the Show page under a Season
 * page), by [route]: back must not lead to a page of an Artwork that may no longer exist.
 */
fun MutableList<NavKey>.replacePagesOf(artworkId: Long, route: Route) {
    removeLastOrNull()
    while (lastOrNull()?.isPageOf(artworkId) == true) removeLastOrNull()
    add(route)
}

private fun NavKey.isPageOf(artworkId: Long): Boolean = when (this) {
    is Route.Show -> this.artworkId == artworkId
    is Route.Season -> this.artworkId == artworkId
    is Route.Artwork -> this.artworkId == artworkId
    else -> false
}
