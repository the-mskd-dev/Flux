package com.mskd.flux.navigation.domain

import com.mskd.flux.ui.text.TextProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.home
import flux.shared.generated.resources.ic_home
import flux.shared.generated.resources.ic_search
import flux.shared.generated.resources.ic_settings
import flux.shared.generated.resources.search
import flux.shared.generated.resources.settings
import org.jetbrains.compose.resources.DrawableResource

enum class BottomBarTab(val route: Route, val iconRes: DrawableResource, val label: TextProvider) {
    CATALOG(Route.Catalog, Res.drawable.ic_home, TextProvider.Resource(Res.string.home)),
    SEARCH(Route.Search(), Res.drawable.ic_search, TextProvider.Resource(Res.string.search)),
    SETTINGS(Route.Settings, Res.drawable.ic_settings, TextProvider.Resource(Res.string.settings)),
}