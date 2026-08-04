package com.mskd.flux.navigation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mskd.flux.navigation.Route
import com.mskd.flux.utils.FluxThemePreview

private enum class BottomBarTab(val route: Route, val icon: ImageVector, val label: String) {
    CATALOG(Route.Catalog, Icons.Outlined.Home, "Home"),
    SEARCH(Route.Search(), Icons.Outlined.Search, "Search"),
    SETTINGS(Route.Settings, Icons.Outlined.Settings, "Settings"),
}

fun Route?.isSameTabAs(target: Route): Boolean = when (target) {
    is Route.Catalog -> this is Route.Catalog
    is Route.Search -> this is Route.Search
    is Route.Settings -> this is Route.Settings
    else -> false
}

fun navigateToTab(backStack: NavBackStack<NavKey>, target: Route) {
    val existingIndex = backStack.indexOfFirst { (it as? Route).isSameTabAs(target) }
    if (existingIndex != -1) {
        while (backStack.size > existingIndex + 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    } else {
        backStack.add(target)
    }
}

@Composable
fun FluxBottomBar(
    currentRoute: Route?,
    onTabSelected: (Route) -> Unit,
) {
    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
    ) {
        BottomBarTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute.isSameTabAs(tab.route),
                onClick = { onTabSelected(tab.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Preview
@Composable
fun FluxBottomBar_Preview() {
    FluxThemePreview{
        FluxBottomBar(
            currentRoute = Route.Catalog,
            onTabSelected = {}
        )
    }
}