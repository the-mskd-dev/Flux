package com.mskd.flux.screens.unknown

import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media

data class UnknownUiState(
    val screen: ScreenState = ScreenState.LOADING,
    val hideProgress: Boolean = false,
    val medias: List<Episode> = emptyList()
)

sealed class UnknownIntent {
    object OnBackTap: UnknownIntent()
    data class PlayMedia(val media: Media): UnknownIntent()
    object OnInfoTap: UnknownIntent()
}

sealed class UnknownEvent {
    object BackToPreviousScreen : UnknownEvent()
    object NavigateToHowToScreen : UnknownEvent()
    data class PlayMedia(val mediaId: Long) : UnknownEvent()
}