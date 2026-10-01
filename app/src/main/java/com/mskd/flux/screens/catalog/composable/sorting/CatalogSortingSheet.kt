package com.mskd.flux.screens.catalog.composable.sorting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.presentation.bottomSheet.FluxBottomSheet
import com.mskd.flux.presentation.bottomSheet.FluxBottomSheetItem
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.resolve
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.sort_by
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSortingSheet(
    isVisible: Boolean,
    selectedMode: CatalogSortingMode,
    sendIntent: (CatalogIntent) -> Unit
) {

    FluxBottomSheet(
        isVisible = isVisible,
        title = stringResource(Res.string.sort_by),
        onDismiss = { sendIntent(CatalogIntent.ShowSortingModes(show = false)) },
        content = {

            CatalogSortingMode.entries.forEach { option ->
                FluxBottomSheetItem(
                    isSelected = option == selectedMode,
                    text = option.description.resolve(),
                    onClick = { sendIntent(CatalogIntent.SelectSortingMode(option)) }
                )
            }

        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@FluxPreview
@Composable
fun CatalogSortingSheet_Preview() {
    FluxThemePreview {
        Box(modifier = Modifier.fillMaxSize()) {
            CatalogSortingSheet(
                isVisible = true,
                selectedMode = CatalogSortingMode.LAST_MODIFICATION,
                sendIntent = {}
            )
        }
    }
}