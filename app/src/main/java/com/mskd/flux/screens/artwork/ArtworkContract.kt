package com.mskd.flux.screens.artwork

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media

@Immutable
data class ArtworkUiState(
    val screen: ScreenState = ScreenState.LOADING,
    val artwork: Artwork = MediaMockups.showArtwork,
    val media: Media = MediaMockups.episode1,
    val episodes: List<Episode> = emptyList(),
    val season: Int = -1,
    val episodePendingConfirmation: Episode? = null,
)

sealed class ArtworkIntent {
    object OnBackTap: ArtworkIntent()
    data class ChangeWatchStatus(val media: Media): ArtworkIntent()
    object MarkPreviousEpisodesAsWatched: ArtworkIntent()
    object CloseEpisodesStatusDialog: ArtworkIntent()
    data class SelectSeason(val season: Int): ArtworkIntent()
    data class PlayMedia(val media: Media, val forceInternal: Boolean = false): ArtworkIntent()
    data class OpenArtworkInfo(val artwork: Artwork): ArtworkIntent()
    data class OpenEpisodeInfo(val episode: Episode): ArtworkIntent()
}

sealed class ArtworkEvent {
    object BackToPreviousScreen : ArtworkEvent()
    data class PlayMedia(val mediaId: Long) : ArtworkEvent()
    data class LaunchExternalPlayer(val media: Media) : ArtworkEvent()
    data class OpenArtworkInfo(val artwork: Artwork) : ArtworkEvent()
    data class OpenEpisodeInfo(val episode: Episode): ArtworkEvent()
}