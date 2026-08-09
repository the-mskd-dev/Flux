package com.mskd.flux.screens.catalog.composable.sorting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.global.FluxBottomSheetItem
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.fillMaxWidthWithLimit
import com.mskd.flux.utils.extensions.resolve
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.sort_by
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSortingSheet(
    selectedMode: CatalogSortingMode,
    sendIntent: (CatalogIntent) -> Unit
) {

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidthWithLimit(),
        onDismissRequest = { sendIntent(CatalogIntent.ShowSortingModes(show = false)) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            Text.List.Title(
                modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                text = stringResource(Res.string.sort_by)
            )

            CatalogSortingMode.entries.forEach { option ->
                FluxBottomSheetItem(
                    isSelected = option == selectedMode,
                    text = option.description.resolve(),
                    onClick = { sendIntent(CatalogIntent.SelectSortingMode(option)) }
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CatalogSortingSheet_Preview() {
    FluxThemePreview {
        Box(modifier = Modifier.fillMaxSize()) {
            CatalogSortingSheet(
                selectedMode = CatalogSortingMode.LAST_MODIFICATION,
                sendIntent = {}
            )
        }
    }
}