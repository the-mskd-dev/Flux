package com.kaem.flux.data.source.artwork

import com.kaem.flux.model.FileSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode

interface ArtworkDataSource {

    suspend fun getArtworks(
        files: List<UserFile> = emptyList(),
        artworkIds: List<Int> = emptyList()
    ) : Pair<List<FluxArtworkSummary>, List<FluxEpisode>>

}