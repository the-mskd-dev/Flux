package com.mskd.flux.navigation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mskd.flux.core.model.core.StringProvider
import com.mskd.flux.navigation.Route
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.resolve
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.home
import flux.shared.generated.resources.search
import flux.shared.generated.resources.settings

private enum class BottomBarTab(val route: Route, val icon: ImageVector, val label: StringProvider) {
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
    currentTab: Route?,
    onTabSelected: (Route) -> Unit,
) {

    Box(
        modifier = Modifier
            .padding(horizontal = FluxUI.Space.medium)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = FluxUI.Space.small)
                .clip(CircleShape)
                .background(NavigationBarDefaults.containerColor)
                .padding(all = FluxUI.Space.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {
            BottomBarTab.entries.forEach { tab ->
                FluxNavigationBarItem(
                    selected = currentTab.isSameTabAs(tab.route),
                    onClick = { onTabSelected(tab.route) },
                    icon = tab.icon,
                    label = tab.label.resolve(),
                )
            }
        }

    }
}

@Composable
fun FluxNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
) {

    val colors = NavigationBarItemDefaults.colors()
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (selected) colors.selectedIndicatorColor else Color.Transparent
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) colors.selectedTextColor else colors.unselectedTextColor
    )

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .background(animatedBackgroundColor)
            .padding(all = FluxUI.Space.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
    ) {

        AnimatedVisibility(
            visible = selected
        ) {
            Icon(
                imageVector = icon,
                tint = colors.selectedIconColor,
                contentDescription = label
            )
        }

        Text.NavigationBarItem(
            text = label,
            color = animatedTextColor
        )

    }



}

@FluxPreview
@Composable
fun FluxBottomBar_Preview() {
    FluxThemePreview{
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            FluxBottomBar(
                currentTab = Route.Catalog,
                onTabSelected = {}
            )
        }

    }
}