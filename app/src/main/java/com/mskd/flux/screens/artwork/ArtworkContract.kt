package com.mskd.flux.screens.artwork

import androidx.compose.runtime.Immutable
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media

@Immutable
data class ArtworkUiState(
    val state: State<FullArtwork> = State.Loading,
    val selectedMedia: Media = MediaMockups.episode1,
    val selectedSeason: Int = -1,
    val episodePendingConfirmation: Episode? = null,
    val useExternalPlayer: Boolean = false,
    val showResetProgressDialog: Boolean = false
)

sealed class ArtworkIntent {
    object OnBackTap: ArtworkIntent()
    data class ChangeWatchStatus(val media: Media): ArtworkIntent()
    object MarkPreviousEpisodesAsWatched: ArtworkIntent()
    object CloseEpisodesStatusDialog: ArtworkIntent()
    data class SelectSeason(val season: Int): ArtworkIntent()
    data class PlayMedia(val media: Media, val forceInternal: Boolean = false): ArtworkIntent()
    data object OpenArtworkInfo: ArtworkIntent()
    data class OpenEpisodeInfo(val episode: Episode): ArtworkIntent()
    data class OnExternalPlayerResult(val progress: Long) : ArtworkIntent()
    data class ShowResetProgressDialog(val show: Boolean) : ArtworkIntent()
    data object ResetProgress: ArtworkIntent()
}

sealed class ArtworkEvent {
    object BackToPreviousScreen : ArtworkEvent()
    data class PlayMedia(val mediaId: Long) : ArtworkEvent()
    data class LaunchExternalPlayer(val media: Media) : ArtworkEvent()
    data class OpenArtworkInfo(val artwork: Artwork) : ArtworkEvent()
    data class OpenEpisodeInfo(val episode: Episode): ArtworkEvent()
}