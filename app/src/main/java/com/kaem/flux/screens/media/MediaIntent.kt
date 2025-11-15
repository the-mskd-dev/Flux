package com.kaem.flux.screens.media

import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media

sealed class MediaIntent {
    object OnBackTap: MediaIntent()
    data class ChangeWatchStatus(val checkPrevious: Boolean): MediaIntent()
    object ChangeWatchStatusForEpisodeAndPrevious: MediaIntent()
    data class SelectSeason(val season: Int): MediaIntent()
    data class SaveWatchTime(val time: Long): MediaIntent()

    data class PlayMedia(val media: Media): MediaIntent()
    object ClosePlayer: MediaIntent()
}