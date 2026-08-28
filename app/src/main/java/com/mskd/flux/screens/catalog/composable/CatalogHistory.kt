package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import org.slf4j.MDC

@Composable
fun CatalogHistory(
    entries: List<HistoryEntry>,
    sendIntent: (CatalogIntent) -> Unit
) {

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
    ) {

        items(items = entries, key = { it.artworkId }) { entry ->

            CatalogHistoryItem(
                entry = entry,
                onClick = {}
            )

        }

    }


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

            FluxImage(
                modifier = Modifier.fillMaxSize(),
                historyEntry = entry,
                contentDescription = entry.title
            )

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
                season = 1,
                number = 1,
                path = "path/to/file",
                duration = 24,
                timestamp = 0L,
            )
        ) { }
    }
}