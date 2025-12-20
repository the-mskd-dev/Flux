package com.kaem.flux.screens.artwork

import androidx.compose.runtime.Immutable
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.model.artwork.Artwork
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Immutable
data class ArtworkUiState(
    val screen: ScreenState = ScreenState.LOADING,
    val artwork: Artwork = MediaMockups.showArtwork,
    val media: Media = MediaMockups.episode1,
    val episodes: List<Episode> = emptyList(),
    val season: Int = -1,
    val episodePendingConfirmation: Episode? = null,
    val playerBackward: Long = 10.seconds.inWholeMilliseconds,
    val playerForward: Long = 10.seconds.inWholeMilliseconds,
    val subtitlesLanguage: Locale = Locale.getDefault()
)

sealed class ArtworkIntent {
    object OnBackTap: ArtworkIntent()
    data class ChangeWatchStatus(val media: Media): ArtworkIntent()
    object MarkPreviousEpisodesAsWatched: ArtworkIntent()
    object CloseEpisodesStatusDialog: ArtworkIntent()
    data class SelectSeason(val season: Int): ArtworkIntent()
    data class SaveWatchTime(val media: Media, val time: Long): ArtworkIntent()

    data class PlayMedia(val media: Media): ArtworkIntent()
}

sealed class ArtworkEvent {
    object BackToPreviousScreen : ArtworkEvent()
    data class PlayMedia(val media: Media) : ArtworkEvent()
}