package com.mskd.flux.screens.player

import androidx.compose.runtime.Immutable
import androidx.media3.common.Player
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media

@Immutable
data class PlayerUiState(
    val state: State<PlayerUiContent> = State.Loading
)

@Immutable
data class PlayerUiContent(

    // DataState
    val fullArtwork: FullArtwork,
    val media: Media,
    val playerRewind: Int = 10,
    val playerForward: Int = 10,
    val player: Player,
    val isPlaying: Boolean = false,
    val duration: Long = 0L,
    val tracks: List<PlayerTrack> = emptyList(),
    val selectedAudio: PlayerTrack? = null,
    val selectedSubtitles: PlayerTrack? = null,

    // UserState
    val showInterface: Boolean = false,
    val isInPip: Boolean = false,
    val seekOverlay: SeekOverlay? = null,
    val ambientOverlay: AmbientOverlay? = null,
    val settingsSheet: SettingsSheet? = null,
    val nextButton: NextButton = NextButton.Hidden,

) {

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

data class PlayerDataState(
    val fullArtwork: FullArtwork,
    val media: Media,
    val player: Player,
    val playerRewind: Int,
    val playerForward: Int,
    val duration: Long,
    val tracks: List<PlayerTrack>,
    val isPlaying: Boolean,
    val selectedAudio: PlayerTrack?,
    val selectedSubtitles: PlayerTrack?,
)

data class PlayerUserState(
    val mediaId: Long,
    val showInterface: Boolean = false,
    val isInPip: Boolean = false,
    val seekOverlay: PlayerUiContent.SeekOverlay? = null,
    val ambientOverlay: PlayerUiContent.AmbientOverlay? = null,
    val settingsSheet: PlayerUiContent.SettingsSheet? = null,
    val nextButton: PlayerUiContent.NextButton = PlayerUiContent.NextButton.Hidden,
)

sealed class PlayerScreen {
    data object Loading : PlayerScreen()
    data object Error : PlayerScreen()
    data class Content(val player: Player, val media: Media) : PlayerScreen()
}

sealed class PlayerIntent {
    data class PlayMedia(val media: Media) : PlayerIntent()
    data object SaveTime : PlayerIntent()
    data object OnBackTap : PlayerIntent()
    data object ChangeInterfaceVisibility : PlayerIntent()
    data object TogglePlayButton : PlayerIntent()
    data object OnFastRewind : PlayerIntent()
    data object OnFastForward : PlayerIntent()
    data class UpdateProgress(val progress: Long) : PlayerIntent()
    data class ShowSettings(val sheet: PlayerUiContent.SettingsSheet?) : PlayerIntent()
    data class SelectTrack(val track: PlayerTrack) : PlayerIntent()
    data class PlayNextEpisode(val episode: Episode) : PlayerIntent()
    data object CancelNextEpisode : PlayerIntent()
    data class OnVolumeChange(val delta: Float) : PlayerIntent()
    data class OnBrightnessChange(val delta: Float) : PlayerIntent()
    data class UpdateAmbientOverlay(val type: PlayerUiContent.AmbientOverlay.Type, val value: Int) : PlayerIntent()
    data object GoToBackground : PlayerIntent()
    data object GoToForeground : PlayerIntent()

    data class OnPipChange(val isInPip: Boolean) : PlayerIntent()
    data object OnClosePiP : PlayerIntent()
}

sealed class PlayerEvent {
    data object BackToPreviousScreen : PlayerEvent()
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

    companion object {
        val NO_SUBTITLES = PlayerTrack(
            id = null,
            label = "",
            language = null,
            type = Type.SUBTITLES
        )
    }

}