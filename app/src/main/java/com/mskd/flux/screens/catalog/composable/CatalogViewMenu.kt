package com.mskd.flux.screens.catalog.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_sort
import flux.shared.generated.resources.sort
import flux.shared.generated.resources.view
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogViewMenu(
    sortingMode: CatalogSortingMode,
    viewMode: CatalogViewMode,
    sendIntent: (CatalogIntent) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall, Alignment.End),
    ) {

        FilledTonalButton(
            onClick = { sendIntent(CatalogIntent.ShowSortingModes(show = true)) },
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_sort),
                contentDescription = stringResource(Res.string.sort)
            )
        }

        FilledTonalButton(
            onClick = { sendIntent(CatalogIntent.ShowViewModes(show = true)) },
        ) {
            AnimatedContent(
                targetState = viewMode
            ) { mode ->
                Icon(
                    painter = painterResource(mode.drawableRes),
                    contentDescription = stringResource(Res.string.view)
                )
            }
        }

    }

}

@Preview
@Composable
fun CatalogViewMenu_Preview() {
    FluxThemePreview {
        CatalogViewMenu(
            sortingMode = CatalogSortingMode.LAST_MODIFICATION,
            viewMode = CatalogViewMode.BY_TYPE
        ) { }
    }
}