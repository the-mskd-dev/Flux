package com.mskd.flux.screens.show.composables

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.palette.graphics.Palette
import coil3.toBitmap
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.Status
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.ui.component.global.FluxImage
import com.mskd.flux.ui.component.global.ProgressStatusBar
import com.mskd.flux.ui.component.global.ProgressStatusChip
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.AppThemePreview
import com.mskd.flux.utils.extensions.grayScale

@Composable
fun SeasonItem(
    modifier: Modifier = Modifier,
    season: Season,
    episodes: List<Episode>,
    onTap: (Int?) -> Unit,
    onLongPress: () -> Unit
) {

    var seedRgb by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .clip(Ui.Shape.itemCard)
                .fillMaxWidth()
                .aspectRatio(Ui.Dimension.itemRatio)
                .combinedClickable(
                    onClick = { onTap(seedRgb) },
                    onLongClick = { onLongPress() }
                ),
            contentAlignment = Alignment.Center
        ) {

            FluxImage(
                modifier = Modifier
                    .matchParentSize()
                    .let { if (episodes.all { e -> e.status == Status.WATCHED }) it.grayScale() else it },
                path = season.imagePath.orEmpty(),
                contentDescription = season.title,
                onSuccess = { state ->
                    val bitmap = state.result.image.toBitmap()
                    Palette.from(bitmap).generate { palette ->
                        seedRgb = palette?.dominantSwatch?.rgb
                    }
                }
            )

            ProgressStatusChip(
                isWatched = episodes.all { it.status == Status.WATCHED }
            )


            ProgressStatusBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                isVisible = episodes.any { it.status == Status.WATCHED } && !episodes.all { it.status == Status.WATCHED } && episodes.size > 1,
                progress = { episodes.count { it.status == Status.WATCHED } / episodes.size.toFloat() }
            )

        }

        Column(
            modifier = Modifier.padding(horizontal = Ui.Space.small, vertical = Ui.Space.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text.Adaptive(
                modifier = Modifier.fillMaxWidth(),
                text = season.title.ifEmpty { stringResource(R.string.season, season.season) },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = MaterialTheme.typography.titleSmall.fontSize,
                    minFontSize = MaterialTheme.typography.labelSmall.fontSize
                ),
            )

            Text.Label.Small(
                modifier = Modifier.fillMaxWidth(),
                text = pluralStringResource(R.plurals.episodes, episodes.size, episodes.size),
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

        }

    }

}

@Preview
@Composable
fun SeasonItem_Preview() {
    AppThemePreview {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.medium)
        ) {

            (1..3).forEach { _ ->
                SeasonItem(
                    modifier = Modifier.weight(1f),
                    season = MediaMockups.season1,
                    episodes = MediaMockups.episodesWithStatus.filter { it.season == MediaMockups.season1.season },
                    onTap = {},
                    onLongPress = {}
                )
            }

        }
    }
}