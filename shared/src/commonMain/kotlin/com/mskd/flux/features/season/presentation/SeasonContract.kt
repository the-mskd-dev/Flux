package com.mskd.flux.features.season.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
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
    val identification: IdentificationAvailability = IdentificationAvailability.HIDDEN,
    val dialog: SeasonDialog? = null,
) {
    /** Title of the Season, or of the Show when the Season has none. */
    val title: String get() = fullShow.seasons.find { it.season == season }?.title?.ifBlank { null } ?: fullShow.artwork.title
}

@Immutable
data class SeasonUiState(
    val state: State<SeasonContent> = State.Loading
)

sealed interface SeasonDialog {
    data class EpisodeStatusConfirmation(val episode: Episode) : SeasonDialog
    data object ResetProgressConfirmation : SeasonDialog
    data object Identification : SeasonDialog
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
    data object ShowIdentificationDialog : SeasonIntent

    // Manual identification
    data class OnIdentified(val artworkId: Long, val type: ContentType) : SeasonIntent

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
    /**
     * The Media now belong to [artworkId], of the given [type]: the navigation replaces the page by the one matching [type].
     * The season is dropped. Back must not lead to the previous Show, which may no longer exist.
     */
    data class Identified(val artworkId: Long, val type: ContentType) : SeasonEvent
}
