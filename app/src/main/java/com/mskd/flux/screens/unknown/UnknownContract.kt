package com.mskd.flux.screens.unknown

import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media

data class UnknownUiState(
    val screen: ScreenState = ScreenState.LOADING,
    val searchQuery: String = "",
    val useExternalPlayer: Boolean = false,
    val medias: List<Episode> = emptyList()
) {

    val filteredMedias get() = medias
        .filter { it.title.contains(searchQuery, true) }

}

sealed class UnknownIntent {
    object OnBackTap: UnknownIntent()
    data class PlayMedia(val media: Media, val forceInternal: Boolean = false): UnknownIntent()
    object OnInfoTap: UnknownIntent()
    data class DoSearch(val query: String) : UnknownIntent()
    data class OnExternalPlayerResult(val progress: Long) : UnknownIntent()
}

sealed class UnknownEvent {
    object BackToPreviousScreen : UnknownEvent()
    object NavigateToHowToScreen : UnknownEvent()
    data class PlayMedia(val mediaId: Long) : UnknownEvent()
    data class LaunchExternalPlayer(val media: Media) : UnknownEvent()
}