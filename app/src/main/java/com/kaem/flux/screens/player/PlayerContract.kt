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
    val controls: Controls = Controls(),
    val tracks: Tracks = Tracks()
) {

    @Immutable
    data class Controls(
        val showInterface: Boolean = false,
        val showSettings: Boolean = false,
    )

    @Immutable
    data class Tracks(
        val tracks: List<PlayerTrack> = emptyList(),
        val selectedAudio: PlayerTrack? = null,
        val selectedSubtitles: PlayerTrack? = null,
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
    data class UpdateTracks(val tracks: List<PlayerTrack>) : PlayerIntent()
    data class SelectTrack(val track: PlayerTrack) : PlayerIntent()
}

sealed class PlayerEvent {
    data object BackToPreviousScreen : PlayerEvent()
    data class SeekRewind(val time: Long) : PlayerEvent()
    data class SeekForward(val time: Long) : PlayerEvent()
    data class UpdateProgress(val progress: Long) : PlayerEvent()
    data object TogglePlayButton : PlayerEvent()
    data class SelectTrack(val track: PlayerTrack) : PlayerEvent()
}

data class PlayerTrack(
    val id: String? = null,
    val name: String,
    val language: String? = null,
    val type: Type
) {

    constructor(language: String, type: Type) : this(
        id = null,
        name = language,
        language = language,
        type = type
    )

    enum class Type {
        AUDIO, SUBTITLES
    }

}