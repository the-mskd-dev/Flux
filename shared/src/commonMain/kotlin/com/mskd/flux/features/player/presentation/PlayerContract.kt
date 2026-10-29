package com.mskd.flux.features.player.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.player.domain.manager.SeekPrecision
import com.mskd.flux.features.player.domain.model.VideoChapter


/** Changes rarely: loaded Media, player, Tracks, duration and skip values. */
@Immutable
data class PlayerSession<out T>(
    val media: Media,
    val player: T,
    val playerRewind: Int = 10,
    val playerForward: Int = 10,
    val duration: Long = 0L,
    val tracks: List<PlayerTrack> = emptyList(),
    val selectedAudio: PlayerTrack? = null,
    val selectedSubtitles: PlayerTrack? = null,
    val chapters: List<VideoChapter> = emptyList(),
) {
    /** A single Chapter is the whole file: the Chapters row is only worth showing from 2. */
    val showChapters: Boolean get() = chapters.size >= 2
}

/** Visibility of the Interface, Settings sheet and Picture-in-Picture mode. */
@Immutable
data class PlayerInterfaceState(
    val showInterface: Boolean = false,
    val isInPip: Boolean = false,
    val settingsSheet: SettingsSheet? = null,
)

/** Transient elements that change often. The Ambient overlay is local to the screen. */
@Immutable
data class PlayerOverlays(
    val seekOverlay: SeekOverlay? = null,
    val chapterButton: ChapterButton? = null,
    val currentChapter: VideoChapter? = null,
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
    data object Chapters : SettingsSheet()
}

sealed interface ChapterButton {
    data class Skip(val chapter: VideoChapter) : ChapterButton
    data class NextEpisode(val nextEpisode: Episode) : ChapterButton
}

sealed class PlayerIntent {
    data object OnBackTap : PlayerIntent()
    data object ChangeInterfaceVisibility : PlayerIntent()
    data object TogglePlayButton : PlayerIntent()
    data object OnFastRewind : PlayerIntent()
    data object OnFastForward : PlayerIntent()
    data class SeekTo(val progress: Long, val precision: SeekPrecision = SeekPrecision.EXACT) : PlayerIntent()
    data class ShowSettings(val sheet: SettingsSheet?) : PlayerIntent()
    data class SelectTrack(val track: PlayerTrack) : PlayerIntent()
    data class SelectChapter(val chapter: VideoChapter) : PlayerIntent()

    data class OnChapterButtonTap(val button: ChapterButton?) : PlayerIntent()
    data class OnVolumeChange(val delta: Float) : PlayerIntent()
    data object GoToBackground : PlayerIntent()
    data object GoToForeground : PlayerIntent()

    data class OnPipChange(val isInPip: Boolean) : PlayerIntent()
    data object OnClosePiP : PlayerIntent()
}

sealed class PlayerEvent {
    data object BackToPreviousScreen : PlayerEvent()
}