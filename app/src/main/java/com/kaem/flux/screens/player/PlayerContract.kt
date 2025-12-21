package com.kaem.flux.screens.player

import androidx.compose.runtime.Immutable
import com.kaem.flux.model.ScreenState
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Immutable
data class PlayerUiState(
    val screen: ScreenState = ScreenState.LOADING,
    val isPlaying: Boolean = false,
    val showInterface: Boolean = false,
    val playerRewind: Long = 10.seconds.inWholeMilliseconds,
    val playerForward: Long = 10.seconds.inWholeMilliseconds,
    val subtitlesLanguage: Locale = Locale.getDefault()
) {

    @Immutable
    data class SubState(
        val isPlaying: Boolean = false,
        val showInterface: Boolean = false
    )

}

sealed class PlayerIntent {
    data class SaveTime(val time: Long) : PlayerIntent()
    data class OnBackTap(val time: Long? = null) : PlayerIntent()
    data class ShowInterface(val show: Boolean) : PlayerIntent()
    data object TogglePlayButton : PlayerIntent()
    data object OnFastRewind : PlayerIntent()
    data object OnFastForward : PlayerIntent()
}

sealed class PlayerEvent {
    data object BackToPreviousScreen : PlayerEvent()
}