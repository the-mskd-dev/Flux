package com.kaem.flux.screens.artwork

import androidx.compose.runtime.Immutable
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Media

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
    data class PlayMedia(val media: Media): ArtworkIntent()
}

sealed class ArtworkEvent {
    object BackToPreviousScreen : ArtworkEvent()
    data class PlayMedia(val media: Media) : ArtworkEvent()
}