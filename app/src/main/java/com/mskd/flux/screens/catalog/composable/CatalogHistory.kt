package com.mskd.flux.screens.catalog.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.features.history.data.mapper.toHistoryEntry
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import com.mskd.flux.ui.component.global.FluxImage
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.EpisodeDropDownMenu
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.bleedHorizontal
import com.mskd.flux.utils.extensions.fillMaxWidthWithLimit
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.continue_watching
import flux.shared.generated.resources.delete
import flux.shared.generated.resources.ic_arrow_down
import flux.shared.generated.resources.ic_delete
import flux.shared.generated.resources.read_less
import flux.shared.generated.resources.read_more
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogHistory(
    entries: List<HistoryEntry>,
    sendIntent: (CatalogIntent) -> Unit
) {

    AnimatedVisibility(
        visible = entries.isNotEmpty()
    ) {

        Column(
            modifier = Modifier
                .bleedHorizontal()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {

            Text.Content.Title(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = FluxUI.Space.medium),
                text = stringResource(Res.string.continue_watching),
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
            ) {

                items(items = entries, key = { it.media.artworkId }) { entry ->

                    CatalogHistoryItem(
                        entry = entry,
                        sendIntent = sendIntent
                    )

                }

            }

        }

    }

}

@Composable
fun CatalogHistoryItem(
    entry: HistoryEntry,
    sendIntent: (CatalogIntent) -> Unit
) {

    var showMenu by remember { mutableStateOf(false) }
    val media = entry.media

    Card(
        modifier = Modifier
            .fillMaxWidthWithLimit(
                fraction = .8f,
                max = 450.dp
            )
            .aspectRatio(FluxUI.Ratio.landscape)
            .combinedClickable(
                onClick = { sendIntent(CatalogIntent.PlayMedia(media = media)) },
                onLongClick = { showMenu = true }
            ),
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
                media = media,
                contentDescription = media.title,
                videoFrame = true
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
                    text = media.title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

            }


        }

    }

    if (showMenu) {
        FluxDropDownMenu(
            onDismissRequest = { showMenu = false },
            items = listOf(
                FluxDropDownMenuItem(
                    text = stringResource(Res.string.delete),
                    onClick = { sendIntent(CatalogIntent.DeleteHistoryEntry(entry = entry)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = stringResource(Res.string.delete)
                        )
                    },
                )
            )
        )
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