package com.mskd.flux.features.unknown.presentation

import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.identification.presentation.IdentificationAvailability

data class UnknownUiState(
    val screen: State<Unit> = State.Loading,
    val searchQuery: String = "",
    val useExternalPlayer: Boolean = false,
    val medias: List<Episode> = emptyList(),
    val identification: IdentificationAvailability = IdentificationAvailability.HIDDEN,
    val dialog: UnknownDialog? = null,
) {

    val filteredMedias get() = medias
        .filter { it.title.contains(searchQuery, true) }

}

sealed interface UnknownDialog {
    data class Identification(val media: Episode) : UnknownDialog
}

sealed interface UnknownIntent {
    object OnBackTap: UnknownIntent
    data class PlayMedia(val media: Media, val forceInternal: Boolean = false): UnknownIntent
    object OnInfoTap: UnknownIntent
    data class OpenFileExplorer(val media: Media): UnknownIntent
    data class DoSearch(val query: String) : UnknownIntent
    data class ShowIdentificationDialog(val media: Episode) : UnknownIntent
    data object CloseDialog : UnknownIntent
    data object OnIdentified : UnknownIntent
}

sealed interface UnknownEvent {
    object BackToPreviousScreen : UnknownEvent
    object NavigateToHowToScreen : UnknownEvent
    data class OpenInternalPlayer(val media: Media) : UnknownEvent
}