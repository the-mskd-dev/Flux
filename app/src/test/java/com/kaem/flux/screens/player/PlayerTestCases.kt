package com.kaem.flux.screens.player

import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.model.artwork.Status

data class ShowSettingsTestCase(
    val description: String,
    val sheet: PlayerUiState.SettingsSheet
)

data class SaveTimeTestCase(
    val description: String,
    val artwork: Artwork,
    val media: Media,
    val time: Long,
    val shouldBeAddedToRecentlyWatched: Boolean,
    val statusExpected: Status
)

data class PlayerBackTapTestCase(
    val description: String,
    val interfaceShowed: Boolean,
)

data class ShowNextEpisodeTestCase(
    val description: String,
    val currentEpisode: Episode,
    val show: Boolean,
    val expectedNexTButton: PlayerUiState.NextButton
)

data class SelectTrackTestCase(
    val description: String,
    val track: PlayerTrack,

)