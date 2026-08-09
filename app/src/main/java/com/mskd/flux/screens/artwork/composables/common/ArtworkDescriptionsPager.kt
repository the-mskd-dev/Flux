package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.media.EpisodesDetails
import com.mskd.flux.ui.component.media.MediaDetailsHorizontal
import com.mskd.flux.ui.component.media.OverviewItem
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.clickableWithBounce
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.no_summary
import flux.shared.generated.resources.summary
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtworkDescriptionsPager(
    fullArtwork: FullArtwork,
    season: Int? = null,
    currentMedia: Media
) {

    val pageCount = when (fullArtwork) {
        is FullArtwork.FullMovie -> 1
        is FullArtwork.FullShow -> if (fullArtwork.isWatching(forSeason = season)) 2 else 1
    }

    var currentPage by remember { mutableIntStateOf(0) }

    AnimatedContent(
        targetState = currentPage,
        transitionSpec = {
            if (targetState > initialState) {
                fadeIn() togetherWith fadeOut()
            } else {
                fadeIn() togetherWith fadeOut()
            }
        }
    ) { i ->

        Card(
            modifier = Modifier
                .padding(horizontal = FluxUI.Space.medium)
                .then(
            if (pageCount > 1) {
                        Modifier.clickableWithBounce {
                            when {
                                currentPage < pageCount - 1 -> currentPage++
                                else -> currentPage--
                            }
                        }
                    } else Modifier
                ),
            shape = FluxUI.shapes.corners
        ) {

            when (fullArtwork) {

                is FullArtwork.FullMovie -> {

                    OverviewItem(
                        title = stringResource(Res.string.summary),
                        description = currentMedia.description,
                        subtitle = { MediaDetailsHorizontal(currentMedia) },
                        topDetails = { GenresTags(genres = fullArtwork.genres) }
                    )

                }

                is FullArtwork.FullShow -> {

                    val episode = currentMedia as Episode

                    if (i > 0 || pageCount == 1) {

                        OverviewItem(
                            title = stringResource(Res.string.summary),
                            description = fullArtwork.artwork.description.ifEmpty { stringResource(Res.string.no_summary) },
                        )

                    } else {

                        OverviewItem(
                            title = episode.title,
                            description = currentMedia.description,
                            topDetails = { EpisodesDetails(episode = episode) },
                            subtitle = { MediaDetailsHorizontal(media = episode) }
                        )

                    }

                }
            }

        }

    }


}



@FluxPreview
@Composable
fun ArtworkDescriptionsPager_Movie_Preview() {
    FluxTheme {
        ArtworkDescriptionsPager(
            fullArtwork = MediaMockups.fullMovie,
            currentMedia = MediaMockups.fullMovie.movie
        )
    }
}

@FluxPreview
@Composable
fun ArtworkDescriptionsPager_Show_Preview() {
    FluxTheme {
        ArtworkDescriptionsPager(
            fullArtwork = MediaMockups.fullShow.copy(episodes = MediaMockups.episodesWithStatus.toImmutableList()),
            currentMedia = MediaMockups.episode1.copy(status = Status.IS_WATCHING)
        )
    }
}