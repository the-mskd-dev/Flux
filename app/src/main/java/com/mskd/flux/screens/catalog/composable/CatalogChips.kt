package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.resolve
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_sort
import flux.shared.generated.resources.sort
import flux.shared.generated.resources.sort_by
import flux.shared.generated.resources.view
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogChips(
    sortingMode: CatalogSortingMode,
    viewMode: CatalogViewMode,
    sendIntent: (CatalogIntent) -> Unit
) {

    Row(
        modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
    ) {

        AssistChip(
            onClick = { sendIntent(CatalogIntent.ShowSortingModes(show = true)) },
            label = { Text.Button.Chip(stringResource(Res.string.sort_by)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_sort),
                    contentDescription = stringResource(Res.string.sort)
                )
            },
            shape = CircleShape,
        )

        AssistChip(
            onClick = { sendIntent(CatalogIntent.ShowViewModes(show = true)) },
            label = { Text.Button.Chip(viewMode.description.resolve()) },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_sort),
                    contentDescription = stringResource(Res.string.view)
                )
            },
            shape = CircleShape,
        )

    }

}

@Preview
@Composable
fun CatalogChips_Preview() {
    FluxThemePreview {
        CatalogChips(
            sortingMode = CatalogSortingMode.LAST_MODIFICATION,
            viewMode = CatalogViewMode.BY_TYPE
        ) { }
    }
}