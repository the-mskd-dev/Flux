package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.ui.component.global.FluxImage
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.fillMaxWidthWithLimit

@Composable
fun CatalogHistory(
    entries: List<HistoryEntry>,
    sendIntent: (CatalogIntent) -> Unit
) {



}

@Composable
fun CatalogHistoryItem(
    entry: HistoryEntry,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidthWithLimit(
                fraction = .95f,
                max = 500.dp
            )
            .aspectRatio(FluxUI.Ratio.landscape),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Cyan
        )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            FluxImage()
        }

    }

}

@FluxPreview
@Composable
fun CatalogHistoryItem_Preview() {
    FluxThemePreview {
        CatalogHistoryItem(
            entry = HistoryEntry(
                id = 0L,
                artworkId = 0L,
                type = ContentType.SHOW,
                title = "Test",
                description = "Test description",
                duration = 24,
                timestamp = 0L,
            )
        ) { }
    }
}