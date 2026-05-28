package com.mskd.flux.screens.artwork

import androidx.compose.runtime.Immutable
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.screens.customization.CustomizationIntent

@Immutable
data class ArtworkUiState(
    val state: State<FullArtwork> = State.Loading,
    val selectedMedia: Media = MediaMockups.episode1,
    val useExternalPlayer: Boolean = false,
    val dialog: ArtworkDialog? = null
)

sealed class ArtworkDialog {
    data class EpisodeStatusConfirmation(val episode: Episode) : ArtworkDialog()
    object ResetProgressConfirmation : ArtworkDialog()
    data class SeasonPreview(val season: Season) : ArtworkDialog()
}

sealed class ArtworkIntent {
    object OnBackTap: ArtworkIntent()
    data class ChangeWatchStatus(val media: Media): ArtworkIntent()
    object MarkPreviousEpisodesAsWatched: ArtworkIntent()
    object CloseEpisodesStatusDialog: ArtworkIntent()
    data class PlayMedia(val media: Media, val forceInternal: Boolean = false): ArtworkIntent()
    data object OpenArtworkInfo: ArtworkIntent()
    data class OpenEpisodeInfo(val episode: Episode): ArtworkIntent()
    data class OnExternalPlayerResult(val progress: Long) : ArtworkIntent()
    data class ShowResetProgressDialog(val show: Boolean) : ArtworkIntent()
    data object ResetProgress: ArtworkIntent()
    data class ShowPreviewForSeason(val season: Season?) : ArtworkIntent()
}

sealed class ArtworkEvent {
    object BackToPreviousScreen : ArtworkEvent()
    data class PlayMedia(val mediaId: Long) : ArtworkEvent()
    data class LaunchExternalPlayer(val media: Media) : ArtworkEvent()
    data class OpenArtworkInfo(val artwork: Artwork) : ArtworkEvent()
    data class OpenEpisodeInfo(val episode: Episode): ArtworkEvent()
}