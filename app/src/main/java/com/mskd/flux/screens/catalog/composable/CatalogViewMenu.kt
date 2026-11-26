package com.mskd.flux.screens.catalog.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
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
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.expressive.ExpressiveRow
import com.mskd.flux.ui.expressive.expressivePress
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.components.buttons.FilterButton
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

    ExpressiveRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Design.Space.extraSmall, Alignment.End),
    ) {

        val [sortRef, viewRef] = createRefs()

        FilterButton(
            modifier = Modifier.expressiveSideBounce(ref = sortRef, right = viewRef),
            onClick = { sendIntent(CatalogIntent.ShowSortingModes(show = true)) },
            painter = painterResource(Res.drawable.ic_sort),
            contentDescription = stringResource(Res.string.sort)
        )

        FilterButton(
            modifier = Modifier.expressiveSideBounce(ref = viewRef, left = sortRef),
            onClick = { sendIntent(CatalogIntent.ShowViewModes(show = true)) },
            painter = painterResource(viewMode.drawableRes),
            contentDescription = stringResource(Res.string.view)
        )

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