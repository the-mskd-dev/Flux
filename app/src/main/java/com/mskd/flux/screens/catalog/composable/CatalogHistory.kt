package com.mskd.flux.screens.catalog.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.features.history.data.mapper.toHistoryEntry
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import com.mskd.flux.ui.component.global.FluxImage
import com.mskd.flux.ui.component.global.ProgressStatusBar
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.ui.theme.LocalUiShapes
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.bleedHorizontal
import com.mskd.flux.utils.extensions.fillMaxWidthWithLimit
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.continue_watching
import flux.shared.generated.resources.delete
import flux.shared.generated.resources.episode
import flux.shared.generated.resources.ic_delete
import flux.shared.generated.resources.remaining_time
import flux.shared.generated.resources.season
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogHistory(
    modifier: Modifier = Modifier,
    entries: List<HistoryEntry>,
    sendIntent: (CatalogIntent) -> Unit
) {

    AnimatedVisibility(
        modifier = modifier
            .bleedHorizontal()
            .fillMaxWidth(),
        visible = entries.isNotEmpty()
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.animateItem(),
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
    modifier: Modifier = Modifier,
    entry: HistoryEntry,
    sendIntent: (CatalogIntent) -> Unit
) {

    var showMenu by remember { mutableStateOf(false) }
    val media = entry.media
    val shape = LocalUiShapes.current.corners

    Card(
        modifier = modifier.fillMaxWidthWithLimit(fraction = .8f, max = 450.dp),
        shape = shape,
        onClick = { sendIntent(CatalogIntent.PlayMedia(media = media)) }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = FluxUI.Space.small),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            Box(
                modifier = Modifier
                    .clip(shape)
                    .fillMaxWidth()
                    .aspectRatio(FluxUI.Ratio.landscape),
                contentAlignment = Alignment.BottomCenter
            ) {

                FluxImage(
                    modifier = Modifier.fillMaxSize(),
                    media = media,
                    contentDescription = media.title,
                    videoFrame = true
                )

                ProgressStatusBar(
                    modifier = Modifier.fillMaxWidth(),
                    isVisible = true,
                    progress = { media.progressPercent },
                )

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FluxUI.Space.small),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
                ) {

                    Text.Card.Title(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface,
                        text = entry.title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                    val remainingTime = (media.duration.minToMs - media.currentTime).timeDescription(withoutSeconds = true)
                    val description = buildString {

                        if (media is Episode) {
                            append("S${media.season}:E${media.number}")
                            append(" • ")
                        }

                        append(stringResource(Res.string.remaining_time, remainingTime))
                    }

                    Text.Card.Label(
                        text = description,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                }

                IconButton(
                    onClick = { showMenu = true },
                    content = {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "menu button"
                        )

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
            entries = MediaMockups
                .allMedias
                .distinctBy { it.artworkId }
                .map { it.toHistoryEntry() }
        ) { }
    }
}