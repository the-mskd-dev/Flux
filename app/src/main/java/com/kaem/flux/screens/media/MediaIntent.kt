package com.kaem.flux.screens.media

import com.kaem.flux.model.media.Episode

sealed class MediaIntent {
    object OnBackTap: MediaIntent()
    object ChangeWatchStatus: MediaIntent()
    data class SelectSeason(val season: Int): MediaIntent()
    data class SelectEpisode(val episode: Episode): MediaIntent()
    data class SaveTime(val time: Long): MediaIntent()
    object ShowPlayer: MediaIntent()
    object ClosePlayer: MediaIntent()
}