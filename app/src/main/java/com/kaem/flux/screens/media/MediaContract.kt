package com.kaem.flux.screens.media

import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.MediaOverview

data class MediaUiState(
    val overview: MediaOverview = MediaOverview(),
    val screen: ScreenState = ScreenState.LOADING,
    val media: Media = MediaMockups.episode1,
    val episodes: List<Episode> = emptyList(),
    val season: Int = -1,
    val showPlayer: Boolean = false,
    val showStatusDialog: Boolean = false,
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