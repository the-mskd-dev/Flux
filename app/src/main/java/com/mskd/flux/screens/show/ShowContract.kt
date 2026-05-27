package com.mskd.flux.screens.show

import androidx.compose.runtime.Immutable
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.screens.artwork.ArtworkDialog

@Immutable
data class ShowUiState(
    val state: State<FullArtwork> = State.Loading,
)

sealed class ShowIntent {
    data class OnSeasonTap(val season: Int) : ShowIntent()
}