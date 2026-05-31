package com.mskd.flux.screens.artwork

import androidx.compose.runtime.Immutable
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media

@Immutable
data class ArtworkDataState(
    val fullArtwork: FullArtwork,
    val useExternalPlayer: Boolean
)

@Immutable
data class ArtworkUserState(
    val selectedMedia: Media? = null,
    val dialog: ArtworkDialog? = null,
)


@Immutable
data class ArtworkContent(
    val fullArtwork: FullArtwork,
    val selectedMedia: Media,
    val selectedSeason: Int?,
    val useExternalPlayer: Boolean,
    val dialog: ArtworkDialog?,
)

@Immutable
data class ArtworkUiState(
    val state: State<ArtworkContent> = State.Loading
)

sealed class ArtworkDialog {
    data class EpisodeStatusConfirmation(val episode: Episode) : ArtworkDialog()
    object ResetProgressConfirmation : ArtworkDialog()
}

sealed class ArtworkIntent {
    //Navigation
    object OnBackTap: ArtworkIntent()
    data class PlayMedia(val media: Media, val forceInternal: Boolean = false): ArtworkIntent()
    data object OpenArtworkInfo: ArtworkIntent()
    data class OpenEpisodeInfo(val episode: Episode): ArtworkIntent()

    // Dialogs
    data object CloseDialog: ArtworkIntent()
    data object ShowResetProgressDialog : ArtworkIntent()

    // Status
    data object ResetProgress: ArtworkIntent()
    data class ChangeWatchStatus(val media: Media): ArtworkIntent()
    object MarkPreviousEpisodesAsWatched: ArtworkIntent()

    // Other
    data class OnExternalPlayerResult(val progress: Long) : ArtworkIntent()
}

sealed class ArtworkEvent {
    object BackToPreviousScreen : ArtworkEvent()
    data class PlayMedia(val mediaId: Long) : ArtworkEvent()
    data class LaunchExternalPlayer(val media: Media) : ArtworkEvent()
    data class OpenUrlInfo(val url: String) : ArtworkEvent()
}