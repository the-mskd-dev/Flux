package com.mskd.flux.features.player.fake

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.player.presentation.PlayerUiContent

object PlayerTestCases {

    data class ShowSettings(
        val description: String,
        val sheet: PlayerUiContent.SettingsSheet
    )

    data class SaveTime(
        val description: String,
        val artwork: Artwork,
        val media: Media,
        val time: Long,
    )

    data class PlayerBackTap(
        val description: String,
        val interfaceShowed: Boolean,
    )

    data class ShowNextEpisode(
        val description: String,
        val currentEpisode: Episode,
        val show: Boolean,
        val expectedNexTButton: PlayerUiContent.NextButton
    )

    data class SelectTrack(
        val description: String,
        val track: PlayerTrack,
    )

    data class UpdateAmbientOverlay(
        val description: String,
        val type: PlayerUiContent.AmbientOverlay.Type,
        val value: Int
    )

}