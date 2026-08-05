package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.features.catalog.domain.model.CatalogSortingOption
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.resolve

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSortingSheet(
    selectedOption: CatalogSortingOption,
    sheetState: SheetState,
    sendIntent: (CatalogIntent) -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = { sendIntent(CatalogIntent.ShowSortingOptions(show = false)) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {
            CatalogSortingOption.entries.forEach { option ->
                CatalogSortingSheetItem(
                    option = option,
                    isSelected = option == selectedOption,
                    sendIntent = sendIntent
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSortingSheetItem(
    option: CatalogSortingOption,
    isSelected: Boolean,
    sendIntent: (CatalogIntent) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { sendIntent(CatalogIntent.SelectSortingOption(option)) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { sendIntent(CatalogIntent.SelectSortingOption(option)) }
        )
        Text.List.Body(text = option.description.resolve())
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CatalogSortingSheet_Preview() {
    FluxThemePreview {
        Box(modifier = Modifier.fillMaxSize()) {
            CatalogSortingSheet(
                selectedOption = CatalogSortingOption.LAST_MODIFICATION,
                sheetState = rememberBottomSheetState(initialValue = SheetValue.Expanded),
                sendIntent = {}
            )
        }
    }
}