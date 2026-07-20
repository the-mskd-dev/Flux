package com.mskd.flux.features.unknown.presentation

import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.core.State

data class UnknownUiState(
    val screen: State<Unit> = State.Loading,
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