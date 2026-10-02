package com.mskd.flux.screens.catalog.composable.viewMode

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.presentation.modal.bottomSheet.FluxBottomSheet
import com.mskd.flux.presentation.modal.bottomSheet.FluxBottomSheetItem
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.view
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogViewModeSheet(
    isVisible: Boolean,
    selectedMode: CatalogViewMode,
    sendIntent: (CatalogIntent) -> Unit
) {

    FluxBottomSheet(
        isVisible = isVisible,
        title = stringResource(Res.string.view),
        onDismiss = { sendIntent(CatalogIntent.ShowViewModes(show = false)) },
        content = {

            CatalogViewMode.entries.forEach { option ->
                FluxBottomSheetItem(
                    isSelected = option == selectedMode,
                    text = option.description.resolve(),
                    onClick = { sendIntent(CatalogIntent.SelectViewMode(option)) }
                )
            }

        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CatalogViewModeSheet_Preview() {
    FluxThemePreview {
        Box(modifier = Modifier.fillMaxSize()) {
            CatalogViewModeSheet(
                isVisible = true,
                selectedMode = CatalogViewMode.BY_TYPE,
                sendIntent = {}
            )
        }
    }
}