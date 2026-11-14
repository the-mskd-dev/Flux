package com.mskd.flux.features.season.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.ArtworkPageType
import com.mskd.flux.features.artwork.ui.composables.ArtworkButtons
import com.mskd.flux.features.artwork.ui.composables.ArtworkImage
import com.mskd.flux.features.artwork.ui.composables.ArtworkOverview
import com.mskd.flux.features.artwork.ui.composables.episodeList
import com.mskd.flux.features.season.presentation.SeasonContent
import com.mskd.flux.features.season.presentation.SeasonIntent
import com.mskd.flux.ui.LandscapePreview
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun SeasonContentLarge(
    content: SeasonContent,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (SeasonIntent) -> Unit,
) {

    val episodesTitleColor = MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(modifier = Modifier.weight(.5f)) {

            ArtworkImage(
                modifier = Modifier.fillMaxSize(),
                fullArtwork = content.fullShow,
                currentMedia = content.selectedEpisode,
                orientation = Orientation.Horizontal,
                isBlurred = content.isArtworkImageBlurred,
                pageType = ArtworkPageType.SEASON,
                onClick = { sendIntent(SeasonIntent.OnArtworkImageClick) }
            )

        }

        LazyColumn(
            modifier = Modifier.weight(.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
        ) {

            item {
                Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateTopPadding()))
            }

            item {
                Text.MainTitle(
                    modifier = Modifier.wrapContentWidth(),
                    text = content.title,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            item {
                ArtworkButtons(
                    media = content.selectedEpisode,
                    onPlay = { sendIntent(SeasonIntent.PlayMedia(media = it)) },
                    onChangeWatchStatus = { sendIntent(SeasonIntent.ChangeWatchStatus(media = it)) },
                )
            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            item {
                ArtworkOverview(
                    overview = content.overview,
                    isSpoiler = content.isOverviewSpoiler,
                    isBlurred = content.isOverviewBlurred,
                    onToggleSpoiler = { sendIntent(SeasonIntent.ToggleOverviewSpoiler) },
                )
            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            episodeList(
                episodes = content.episodes,
                selectedMediaId = content.selectedEpisode.mediaId,
                expandedEpisodeId = content.expandedEpisodeId,
                spoilerEpisodeIds = content.spoilerEpisodeIds,
                blurredEpisodeIds = content.blurredEpisodeIds,
                titleColor = episodesTitleColor,
                actions = seasonEpisodeActions(sendIntent),
            )

            item {
                Spacer(modifier = Modifier
                    .padding(bottom = FluxUI.Space.bottomScreen)
                    .height(scaffoldInnerPadding.calculateBottomPadding())
                )
            }

        }

    }

}

@LandscapePreview
@Composable
fun SeasonContentLarge_Preview() {
    FluxTheme {
        SeasonContentLarge(
            content = previewSeasonContent,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}
