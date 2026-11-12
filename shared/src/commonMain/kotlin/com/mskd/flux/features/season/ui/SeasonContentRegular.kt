package com.mskd.flux.features.season.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.ArtworkPageType
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.artwork.ui.composables.ArtworkButtons
import com.mskd.flux.features.artwork.ui.composables.ArtworkOverview
import com.mskd.flux.features.artwork.ui.composables.ArtworkHeader
import com.mskd.flux.features.artwork.ui.composables.episodeList
import com.mskd.flux.features.season.presentation.SeasonContent
import com.mskd.flux.features.season.presentation.SeasonIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.PortraitPreview
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SeasonContentRegular(
    content: SeasonContent,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (SeasonIntent) -> Unit,
) {

    val episodesTitleColor = MaterialTheme.colorScheme.onBackground

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item {

            ArtworkHeader(
                modifier = Modifier.fillMaxWidth(),
                fullArtwork = content.fullShow,
                currentMedia = content.selectedEpisode,
                showTitle = false,
                pageType = ArtworkPageType.SEASON,
                isImageBlurred = content.isArtworkImageBlurred,
                // The content is already inset by the page padding
                titleHorizontalMargin = FluxUI.Space.medium * 2,
                onImageClick = { sendIntent(SeasonIntent.OnArtworkImageClick) },
                buttons = { modifier ->
                    ArtworkButtons(
                        modifier = modifier,
                        media = content.selectedEpisode,
                        onPlay = { sendIntent(SeasonIntent.PlayMedia(media = it)) },
                        onChangeWatchStatus = { sendIntent(SeasonIntent.ChangeWatchStatus(media = it)) },
                    )
                }
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

internal val previewSeasonContent = SeasonContent(
    fullShow = MediaMockups.fullShow,
    season = 1,
    episodes = persistentListOf(MediaMockups.episode1, MediaMockups.episode2),
    selectedEpisode = MediaMockups.episode1,
    overview = ArtworkOverviewContent.Summary(MediaMockups.season1.description),
)

@PortraitPreview
@Composable
fun SeasonContentRegular_Preview() {
    FluxTheme {
        SeasonContentRegular(
            content = previewSeasonContent,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}
