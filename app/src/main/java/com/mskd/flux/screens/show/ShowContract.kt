package com.mskd.flux.screens.show

import androidx.compose.runtime.Immutable
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.screens.artwork.ArtworkDialog
import com.mskd.flux.screens.artwork.ArtworkIntent

@Immutable
data class ShowUiState(
    val state: State<FullArtwork> = State.Loading,
    val dialog: ShowDialog? = null
)

sealed class ShowDialog {
    data class SeasonPreview(val season: Season) : ShowDialog()
}

sealed class ShowIntent {
    object OnBackTap: ShowIntent()
    data class OnSeasonTap(val season: Int, val rgb: Int?) : ShowIntent()
    data class ShowSeasonPreview(val season: Season) : ShowIntent()
    data object CloseDialog : ShowIntent()
}

sealed class ShowEvent {
    object BackToPreviousScreen : ShowEvent()
    data class NavigateToSeason(val artworkId: Long, val season: Int, val rgb: Int?) : ShowEvent()
}