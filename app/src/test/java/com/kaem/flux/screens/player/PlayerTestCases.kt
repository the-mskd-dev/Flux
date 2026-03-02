package com.kaem.flux.screens.player

import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.Media

data class ShowSettingsTestCase(
    val description: String,
    val sheet: PlayerUiState.SettingsSheet
)

data class SaveTimeTestCase(
    val description: String,
    val artwork: Artwork,
    val media: Media,
    val time: Long,
    val shouldBeRemovedFromRecentlyWatched: Boolean,
    val shouldBeAddedFromRecentlyWatched: Boolean
)