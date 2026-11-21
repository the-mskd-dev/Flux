package com.mskd.flux.features.season.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Immutable
data class SeasonContent(
    val fullShow: FullArtwork.FullShow,
    val season: Int,
    /** The Episode of [season] only, sorted by number. */
    val episodes: ImmutableList<Episode>,
    val selectedEpisode: Episode,
    val overview: ArtworkOverviewContent,
    val isOverviewSpoiler: Boolean = false,
    val isOverviewBlurred: Boolean = false,
    val expandedEpisodeId: Long? = null,
    val spoilerEpisodeIds: ImmutableSet<Long> = persistentSetOf(),
    val blurredEpisodeIds: ImmutableSet<Long> = persistentSetOf(),
    /** The Artwork image shows the selected Episode: it is hidden while that Episode is. */
    val isArtworkImageBlurred: Boolean = false,
    val dialog: SeasonPageDialog? = null,
) {
    /** Title of the Season, or of the Show when the Season has none. */
    val title: String get() = fullShow.season(season)?.title?.ifBlank { null } ?: fullShow.artwork.title
}

/** The Season [number] of this Show, when TMDB knows it: an Episode can exist without a matching Season. */
internal fun FullArtwork.FullShow.season(number: Int): Season? = seasons.find { it.season == number }

@Immutable
data class SeasonUiState(
    val state: State<SeasonContent> = State.Loading
)

sealed interface SeasonPageDialog {
    data class EpisodeStatusConfirmation(val episode: Episode) : SeasonPageDialog
    data object ResetProgressConfirmation : SeasonPageDialog
}

sealed interface SeasonIntent {
    // Navigation
    data object OnBackTap : SeasonIntent
    data class PlayMedia(val media: Media, val forceInternal: Boolean = false) : SeasonIntent
    data object OpenSeasonInfo : SeasonIntent
    data class OpenFileExplorer(val media: Media) : SeasonIntent
    data class OpenEpisodeInfo(val episode: Episode) : SeasonIntent
    data object OnArtworkImageClick : SeasonIntent

    // Dialogs
    data object CloseDialog : SeasonIntent
    data object ShowResetProgressDialog : SeasonIntent

    // Status
    data object ResetProgress : SeasonIntent
    data class ChangeWatchStatus(val media: Media) : SeasonIntent
    data object MarkPreviousEpisodesAsWatched : SeasonIntent

    // Other
    data class OnExternalPlayerResult(val progress: Long) : SeasonIntent
    data class ExpandEpisodeDescription(val episode: Episode) : SeasonIntent
    data object CollapseEpisodeDescription : SeasonIntent
    data class ToggleEpisodeSpoiler(val episode: Episode) : SeasonIntent
    data object ToggleOverviewSpoiler : SeasonIntent
}

sealed interface SeasonEvent {
    data object BackToPreviousScreen : SeasonEvent
    data class PlayMedia(val media: Media, val externalPlayer: Boolean) : SeasonEvent
    data class NavigateToGallery(val artworkId: Long, val contentType: ContentType) : SeasonEvent
}
