package com.kaem.flux.screens.media

import com.kaem.flux.model.media.Episode

sealed class MediaIntent {
    object OnBackTap: MediaIntent()
    data class ChangeWatchStatus(val checkPrevious: Boolean): MediaIntent()
    object ChangeWatchStatusForEpisodeAndPrevious: MediaIntent()
    data class SelectSeason(val season: Int): MediaIntent()
    data class SelectEpisode(val episode: Episode): MediaIntent()
    data class SaveTime(val time: Long): MediaIntent()
    object ShowPlayer: MediaIntent()
    object ClosePlayer: MediaIntent()
}