package com.mskd.flux.features.player.fake

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.presentation.AmbientOverlay
import com.mskd.flux.features.player.presentation.ChapterButton
import com.mskd.flux.features.player.presentation.SettingsSheet

object PlayerTestCases {

    data class ShowSettings(
        val description: String,
        val sheet: SettingsSheet
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

    data class ChapterButtonTest(
        val description: String,
        val chapters: List<VideoChapter>,
        val progress: Long,
        val expectedChapterButton: ChapterButton?
    )

    data class SelectTrack(
        val description: String,
        val track: PlayerTrack,
    )

    data class UpdateAmbientOverlay(
        val description: String,
        val type: AmbientOverlay.Type,
        val value: Int
    )

}