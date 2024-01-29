package com.kaem.flux.data.source.artwork

import com.kaem.flux.model.FileSource
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode

interface ArtworkDataSource {

    suspend fun getArtworks(
        files: List<FileSource>,
        artworkIds: List<Int>
    ) : Pair<List<FluxArtworkSummary>, List<FluxEpisode>>

}