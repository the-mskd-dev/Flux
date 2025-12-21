package com.kaem.flux.screens.player

import androidx.compose.runtime.Immutable
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Media
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Immutable
data class PlayerUiState(
    val screen: ScreenState = ScreenState.LOADING,
    val showInterface: Boolean = false,
    val playerBackward: Long = 10.seconds.inWholeMilliseconds,
    val playerForward: Long = 10.seconds.inWholeMilliseconds,
    val subtitlesLanguage: Locale = Locale.getDefault()
)

sealed class PlayerIntent {
    data class SaveTime(val time: Long) : PlayerIntent()
    data class OnBackTap(val time: Long? = null) : PlayerIntent()
    data class ShowInterface(val show: Boolean) : PlayerIntent()
}

sealed class PlayerEvent {
    data object BackToPreviousScreen : PlayerEvent()
}