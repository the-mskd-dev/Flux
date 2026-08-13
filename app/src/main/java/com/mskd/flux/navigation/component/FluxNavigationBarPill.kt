package com.mskd.flux.navigation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.mskd.flux.navigation.domain.BottomBarTab
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.domain.isSameTabAs
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.resolve
import org.jetbrains.compose.resources.painterResource

@Composable
fun FluxNavigationBarPill(
    currentTab: Route?,
    onTabSelected: (Route) -> Unit,
) {

    Box(
        modifier = Modifier
            .padding(horizontal = FluxUI.Space.medium)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Surface(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = FluxUI.Space.medium),
            shadowElevation = FluxUI.Elevation.navigationBar,
            shape = CircleShape,
            color = NavigationBarDefaults.containerColor,
            tonalElevation = NavigationBarDefaults.Elevation
        ) {

            Row(
                modifier = Modifier
                    .padding(all = FluxUI.Space.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {
                BottomBarTab.entries.forEach { tab ->
                    FluxNavigationBarPillItem(
                        selected = currentTab.isSameTabAs(tab.route),
                        onClick = { onTabSelected(tab.route) },
                        icon = painterResource(tab.iconRes),
                        label = tab.label.resolve(),
                    )
                }
            }

        }

    }
}

@Composable
fun FluxNavigationBarPillItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: Painter,
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
            .padding(all = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
    ) {

        AnimatedVisibility(
            visible = selected
        ) {
            Icon(
                painter = icon,
                tint = colors.selectedIconColor,
                contentDescription = label
            )
        }

        Text.Button.NavigationBarItem(
            text = label,
            color = animatedTextColor
        )

    }



}

@FluxPreview
@Composable
fun FluxNavigationBar_Pill_Preview() {
    FluxThemePreview{
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.BottomCenter
        ) {
            FluxNavigationBarPill(
                currentTab = Route.Catalog,
                onTabSelected = {}
            )
        }

    }
}