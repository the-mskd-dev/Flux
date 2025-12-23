package com.kaem.flux.screens.player

import androidx.compose.runtime.Immutable
import com.kaem.flux.model.artwork.Media
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Immutable
data class PlayerUiState(
    val screen: PlayerScreen = PlayerScreen.Loading,
    val playerRewind: Long = 10.seconds.inWholeMilliseconds,
    val playerForward: Long = 10.seconds.inWholeMilliseconds,
    val subtitlesLanguage: Locale = Locale.getDefault(),
    val controls: Controls = Controls(),
) {

    @Immutable
    data class Controls(
        val showInterface: Boolean = false,
        val showSettings: Boolean = false,
    )

}

sealed class PlayerScreen {
    data object Loading : PlayerScreen()
    data object Error : PlayerScreen()
    data class Content(val media: Media) : PlayerScreen()
}

sealed class PlayerIntent {
    data class SaveTime(val time: Long) : PlayerIntent()
    data class OnBackTap(val time: Long? = null) : PlayerIntent()
    data object ShowInterface : PlayerIntent()
    data object TogglePlayButton : PlayerIntent()
    data object OnFastRewind : PlayerIntent()
    data object OnFastForward : PlayerIntent()
    data class UpdateProgress(val progress: Long) : PlayerIntent()
    data object ShowSettings : PlayerIntent()
}

sealed class PlayerEvent {
    data object BackToPreviousScreen : PlayerEvent()
    data class SeekRewind(val time: Long) : PlayerEvent()
    data class SeekForward(val time: Long) : PlayerEvent()
    data class UpdateProgress(val progress: Long) : PlayerEvent()
    data object TogglePlayButton : PlayerEvent()
}