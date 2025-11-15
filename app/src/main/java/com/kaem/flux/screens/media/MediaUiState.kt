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
    val showEpisodesSheet: Boolean = false
)