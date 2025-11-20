package com.kaem.flux.screens.media

import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.MediaOverview

data class MediaUiState(
    val overview: MediaOverview = MediaOverview(),
    val screen: ScreenState = ScreenState.LOADING,
    val selectedMedia: Media? = null,
    val episodes: List<Episode> = emptyList(),
    val currentSeason: Int = -1,
    val showPlayer: Boolean = false,
    val showStatusDialog: Boolean = false,
)

sealed class MediaIntent {
    object OnBackTap: MediaIntent()
    data class ChangeWatchStatus(val checkPrevious: Boolean, val episode: Episode? = null): MediaIntent()
    object ChangeWatchStatusForEpisodeAndPrevious: MediaIntent()
    data class SelectSeason(val season: Int): MediaIntent()
    data class SaveWatchTime(val time: Long): MediaIntent()

    data class PlayMedia(val media: Media): MediaIntent()
    object ClosePlayer: MediaIntent()
}

sealed class MediaEvent {
    object BackToPreviousScreen : MediaEvent()
}