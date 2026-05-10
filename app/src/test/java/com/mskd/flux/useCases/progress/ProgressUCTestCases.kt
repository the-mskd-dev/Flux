package com.mskd.flux.useCases.progress

import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status

object ProgressUCTestCases {

    data class SaveProgress(
        val description: String,
        val artwork: Artwork,
        val media: Media,
        val progress: Long,
        val shouldBeAddedToRecentlyWatched: Boolean,
        val statusExpected: Status
    )

    data class ChangeStatus(
        val description: String,
        val media: Media,
        val status: Status,
        val expectedRemoveFromRecentlyWatched: Boolean
    )

    data class ResetProgress(
        val description: String,
        val artwork: Artwork,
    )

}