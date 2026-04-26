package com.mskd.flux.screens.player

import androidx.compose.runtime.Immutable
import androidx.media3.common.text.Cue
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media

@Immutable
data class PlayerUiState(
    val screen: PlayerScreen = PlayerScreen.Loading,
    val playerRewind: Int = 10,
    val playerForward: Int = 10,
    val controls: Controls = Controls(),
    val tracks: Tracks = Tracks(),
    val seekOverlay: SeekOverlay? = null,
    val ambientOverlay: AmbientOverlay? = null,
) {

    val media: Media? get() = (screen as? PlayerScreen.Content)?.media

    @Immutable
    data class Controls(
        val isPlaying: Boolean = false,
        val showInterface: Boolean = false,
        val settingsSheet: SettingsSheet? = null,
        val nextButton: NextButton = NextButton.Hidden,
        val progress: Long = 0L
    )

    @Immutable
    data class Tracks(
        val tracks: List<PlayerTrack> = emptyList(),
        val selectedAudio: PlayerTrack? = null,
        val selectedSubtitles: PlayerTrack? = null,
        val subtitles: List<Cue> = emptyList()
    )

    @Immutable
    data class SeekOverlay(
        val amount: Int,
        val type: Type
    ) {
        enum class Type { REWIND, FORWARD }
    }

    @Immutable
    data class AmbientOverlay(
        val value: Int,
        val type: Type
    ) {
        enum class Type { BRIGHTNESS, VOLUME }
    }

    sealed class SettingsSheet {
        data object Settings : SettingsSheet()
        data class Tracks(val type: PlayerTrack.Type) : SettingsSheet()
    }

    sealed class NextButton {
        data class Showed(val episode: Episode) : NextButton()
        data object Hidden : NextButton()
        data object Canceled : NextButton()
    }

}

sealed class PlayerScreen {
    data object Loading : PlayerScreen()
    data object Error : PlayerScreen()
    data class Content(val media: Media) : PlayerScreen()
}

sealed class PlayerIntent {
    data class PlayMedia(val media: Media) : PlayerIntent()
    data class SaveTime(val time: Long) : PlayerIntent()
    data class OnBackTap(val time: Long? = null) : PlayerIntent()
    data object ChangeInterfaceVisibility : PlayerIntent()
    data object TogglePlayButton : PlayerIntent()
    data object OnFastRewind : PlayerIntent()
    data object OnFastForward : PlayerIntent()
    data class UpdateProgress(val progress: Long) : PlayerIntent()
    data class ShowSettings(val sheet: PlayerUiState.SettingsSheet?) : PlayerIntent()
    data class SelectTrack(val track: PlayerTrack) : PlayerIntent()
    data class PlayNextEpisode(val episode: Episode) : PlayerIntent()
    data object CancelNextEpisode : PlayerIntent()
    data class OnVolumeChange(val delta: Float) : PlayerIntent()
    data class OnBrightnessChange(val delta: Float) : PlayerIntent()
    data class UpdateAmbientOverlay(val type: PlayerUiState.AmbientOverlay.Type, val value: Int) : PlayerIntent()
}

sealed class PlayerEvent {
    data object BackToPreviousScreen : PlayerEvent()
    data object SaveTimeRequested : PlayerEvent()
    data class ChangeBrightness(val delta: Float) : PlayerEvent()
}

data class PlayerTrack(
    val id: String? = null,
    val label: String,
    val language: String? = null,
    val type: Type
) {

    enum class Type {
        AUDIO, SUBTITLES
    }

}