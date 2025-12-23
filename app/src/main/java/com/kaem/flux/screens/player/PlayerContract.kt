package com.kaem.flux.screens.player

import androidx.compose.runtime.Immutable
import androidx.media3.common.text.Cue
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Media
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Immutable
data class PlayerUiState(
    val screen: PlayerScreen = PlayerScreen.Loading,
    val playerRewind: Long = 10.seconds.inWholeMilliseconds,
    val playerForward: Long = 10.seconds.inWholeMilliseconds,
    val subtitlesLanguage: Locale = Locale.getDefault(),
    val isPlaying: Boolean = false,
    val showInterface: Boolean = false,
    val showSettings: Boolean = false,
    val subtitles: List<Cue> = emptyList()
) {

    @Immutable
    data class Interface(
        val isPlaying: Boolean = false,
        val showInterface: Boolean = false,
        val showSettings: Boolean = false,
    )

    @Immutable
    data class Subtitles(
        val subtitles: List<Cue> = emptyList()
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
}