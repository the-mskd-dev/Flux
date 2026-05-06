package com.mskd.flux.useCases

import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status

object ArtworkProgressUCTestCases {

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
        val artworkContent: ArtworkRepository.Content,
        val expectedRemoveFromRecentlyWatched: Boolean
    )

}