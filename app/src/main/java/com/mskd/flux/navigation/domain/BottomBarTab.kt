package com.mskd.flux.navigation.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.mskd.flux.core.model.core.StringProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.home
import flux.shared.generated.resources.search
import flux.shared.generated.resources.settings

enum class BottomBarTab(val route: Route, val icon: ImageVector, val label: StringProvider) {
    CATALOG(Route.Catalog, Icons.Outlined.Home, StringProvider.Resource(Res.string.home)),
    SEARCH(Route.Search(), Icons.Outlined.Search, StringProvider.Resource(Res.string.search)),
    SETTINGS(Route.Settings, Icons.Outlined.Settings, StringProvider.Resource(Res.string.settings)),
}

fun Route?.isSameTabAs(target: Route): Boolean = when (target) {
    is Route.Catalog -> this is Route.Catalog
    is Route.Search -> this is Route.Search
    is Route.Settings -> this is Route.Settings
    else -> false
}

fun navigateToTab(
    backStack: MutableList<NavKey>,
    target: Route,
) {
    val current = backStack.lastOrNull() as? Route
    if (current.isSameTabAs(target)) return

    val existingIndex = backStack.indexOfFirst { (it as? Route).isSameTabAs(target) }
    if (existingIndex != -1) {
        while (backStack.size > existingIndex + 1) backStack.removeAt(backStack.lastIndex)
    } else {
        backStack.add(target)
    }
}