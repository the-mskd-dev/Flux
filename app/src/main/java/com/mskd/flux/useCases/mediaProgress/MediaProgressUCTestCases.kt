package com.mskd.flux.useCases.mediaProgress

import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status

object MediaProgressUCTestCases {

    data class SaveProgress(
        val description: String,
        val artwork: Artwork,
        val media: Media,
        val progress: Long,
        val shouldBeAddedToRecentlyWatched: Boolean,
        val statusExpected: Status
    )

}