package com.mskd.flux.features.artwork.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.core.State
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Immutable
data class ArtworkDataState(
    val fullArtwork: FullArtwork,
    val useExternalPlayer: Boolean,
    val blurSpoilers: Boolean,
    val people: ImmutableList<Person> = persistentListOf()
)

@Immutable
data class ArtworkUserState(
    val selectedMedia: Media? = null,
    val expandedEpisodeId: Long? = null,
    val revealedEpisodeIds: ImmutableSet<Long> = persistentSetOf(),
    val isOverviewRevealed: Boolean = false,
    val dialog: ArtworkDialog? = null,
)

@Immutable
data class ArtworkContent(
    val fullArtwork: FullArtwork,
    val selectedMedia: Media,
    val selectedSeason: Int?,
    val overview: ArtworkOverviewContent,
    val isOverviewSpoiler: Boolean = false,
    val isOverviewBlurred: Boolean = false,
    val expandedEpisodeId: Long? = null,
    val spoilerEpisodeIds: ImmutableSet<Long> = persistentSetOf(),
    val blurredEpisodeIds: ImmutableSet<Long> = persistentSetOf(),
    val useExternalPlayer: Boolean,
    val people: ImmutableList<Person> = persistentListOf(),
    val dialog: ArtworkDialog?,
)

@Immutable
data class ArtworkUiState(
    val state: State<ArtworkContent> = State.Loading
)

sealed class ArtworkDialog {
    data class EpisodeStatusConfirmation(val episode: Episode) : ArtworkDialog()
    object ResetProgressConfirmation : ArtworkDialog()
}

sealed interface ArtworkIntent {
    // Navigation
    object OnBackTap: ArtworkIntent
    data class PlayMedia(val media: Media, val forceInternal: Boolean = false): ArtworkIntent
    data object OpenArtworkInfo: ArtworkIntent
    data class OpenFileExplorer(val media: Media): ArtworkIntent
    data class OpenEpisodeInfo(val episode: Episode): ArtworkIntent
    data class OnPersonClick(val person: Person): ArtworkIntent
    data object OnArtworkImageClick : ArtworkIntent

    // Dialogs
    data object CloseDialog: ArtworkIntent
    data object ShowResetProgressDialog : ArtworkIntent

    // Status
    data object ResetProgress: ArtworkIntent
    data class ChangeWatchStatus(val media: Media): ArtworkIntent
    object MarkPreviousEpisodesAsWatched: ArtworkIntent

    // Other
    data class OnExternalPlayerResult(val progress: Long) : ArtworkIntent
    data class ExpandEpisodeDescription(val episode: Episode) : ArtworkIntent
    data object CollapseEpisodeDescription : ArtworkIntent
    data class ToggleEpisodeSpoiler(val episode: Episode) : ArtworkIntent
    data object ToggleOverviewSpoiler : ArtworkIntent
}

sealed interface ArtworkEvent {
    object BackToPreviousScreen : ArtworkEvent
    data class PlayMedia(val media: Media, val externalPlayer: Boolean) : ArtworkEvent
    data class NavigateToPerson(val person: Person) : ArtworkEvent
    data class NavigateToGallery(val artworkId: Long, val contentType: ContentType) : ArtworkEvent
}