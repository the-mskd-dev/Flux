package com.mskd.flux.navigation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.navigation.domain.BottomBarTab
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.domain.isSameTabAs
import com.mskd.flux.ui.component.global.FluxDivider
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.resolve
import org.jetbrains.compose.resources.painterResource
import java.nio.file.WatchEvent

@Composable
fun FluxNavigationBarBasic(
    currentTab: Route?,
    onTabSelected: (Route) -> Unit,
) {

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {

        BottomBarTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentTab.isSameTabAs(tab.route),
                onClick = { onTabSelected(tab.route) },
                icon = {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = tab.label.resolve()
                    )
                },
                label = {
                    Text.Button.NavigationBarItem(text = tab.label.resolve())
                }
            )
        }

    }

}

@FluxPreview
@Composable
fun FluxNavigationBar_Basic_Preview() {
    FluxThemePreview{
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.BottomCenter
        ) {
            FluxNavigationBarBasic(
                currentTab = Route.Catalog,
                onTabSelected = {}
            )
        }

    }
}