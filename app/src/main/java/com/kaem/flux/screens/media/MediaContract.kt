package com.kaem.flux.screens.media

import androidx.compose.runtime.Immutable
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.MediaOverview
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Immutable
data class MediaUiState(
    val screen: ScreenState = ScreenState.LOADING,
    val overview: MediaOverview = MediaOverview(),
    val media: Media = MediaMockups.episode1,
    val episodes: List<Episode> = emptyList(),
    val season: Int = -1,
    val showPlayer: Boolean = false,
    val showStatusDialog: Boolean = false,
    val playerBackward: Long = 10.seconds.inWholeMilliseconds,
    val playerForward: Long = 10.seconds.inWholeMilliseconds,
    val subtitlesLanguage: Locale = Locale.getDefault()
)

sealed class MediaIntent {
    object OnBackTap: MediaIntent()
    data class ChangeWatchStatus(val media: Media): MediaIntent()
    object MarkPreviousEpisodesAsWatched: MediaIntent()
    object CloseEpisodesStatusDialog: MediaIntent()
    data class SelectSeason(val season: Int): MediaIntent()
    data class SaveWatchTime(val time: Long): MediaIntent()

    data class PlayMedia(val media: Media): MediaIntent()
    object ClosePlayer: MediaIntent()
}

sealed class MediaEvent {
    object BackToPreviousScreen : MediaEvent()
}