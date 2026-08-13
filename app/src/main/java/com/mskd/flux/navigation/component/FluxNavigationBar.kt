package com.mskd.flux.navigation.component

import androidx.compose.runtime.Composable
import com.mskd.flux.features.customization.domain.model.NavigationStyle
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.ui.theme.LocalUiGlobal

@Composable
fun FluxNavigationBar(
    currentTab: Route?,
    onTabSelected: (Route) -> Unit,
) {

    val navigationStyle = LocalUiGlobal.current.navigationStyle

    if (navigationStyle == NavigationStyle.PILL) {
        FluxNavigationBarPill(
            currentTab = currentTab,
            onTabSelected = onTabSelected
        )
    } else {
        FluxNavigationBarBasic(
            currentTab = currentTab,
            onTabSelected = onTabSelected
        )
    }

}