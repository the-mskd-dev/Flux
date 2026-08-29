package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.features.history.data.mapper.toHistoryEntry
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.global.FluxImage
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.fillMaxWidthWithLimit
import com.mskd.flux.utils.itemWidthFor
import org.slf4j.MDC

@Composable
fun CatalogHistory(
    entries: List<HistoryEntry>,
    sendIntent: (CatalogIntent) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
    ) {

        Text.Content.Title(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = FluxUI.Space.medium),
            text = "Reprendre",
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
        ) {

            items(items = entries, key = { it.artworkId to it.id }) { entry ->

                CatalogHistoryItem(
                    entry = entry,
                    onClick = {}
                )

            }

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
                fraction = .8f,
                max = 500.dp
            )
            .aspectRatio(FluxUI.Ratio.landscape),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Cyan
        )
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {

            FluxImage(
                modifier = Modifier.fillMaxSize(),
                historyEntry = entry,
                contentDescription = entry.title
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(all = FluxUI.Space.small),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {

                Text.Content.Body(
                    modifier = Modifier.fillMaxWidth(),
                    text = entry.title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

            }


        }

    }

}

@FluxPreview
@Composable
fun CatalogHistoryItem_Preview() {
    FluxThemePreview {
        CatalogHistory(
            entries = MediaMockups.episodes.map { it.toHistoryEntry() }
        ) { }
    }
}