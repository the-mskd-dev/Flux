package com.kaem.flux.screens.media

import com.kaem.flux.model.media.Episode
import com.kaem.flux.screens.category.CategoryIntent

sealed class MediaIntent {
    object OnBackTap: MediaIntent()
    object ChangeWatchStatus: MediaIntent()
    data class SelectSeason(val season: Int): MediaIntent()
    data class SelectEpisode(val episode: Episode): MediaIntent()
    object PlayMedia: MediaIntent()
}