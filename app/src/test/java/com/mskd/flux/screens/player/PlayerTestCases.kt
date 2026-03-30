package com.mskd.flux.screens.player

import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status

object PlayerTestCases {

    data class ShowSettings(
        val description: String,
        val sheet: PlayerUiState.SettingsSheet
    )

    data class SaveTime(
        val description: String,
        val artwork: Artwork,
        val media: Media,
        val time: Long,
        val shouldBeAddedToRecentlyWatched: Boolean,
        val statusExpected: Status
    )

    data class PlayerBackTap(
        val description: String,
        val interfaceShowed: Boolean,
        val backSystem: Boolean
    )

    data class ShowNextEpisode(
        val description: String,
        val currentEpisode: Episode,
        val show: Boolean,
        val expectedNexTButton: PlayerUiState.NextButton
    )

    data class SelectTrack(
        val description: String,
        val track: PlayerTrack,

        )

}