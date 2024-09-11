package com.kaem.flux.data.source.artwork

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.Episode

interface ArtworkDataSource {

    suspend fun getArtworks(
        files: List<UserFile> = emptyList(),
        artworkIds: List<Int> = emptyList()
    ) : Pair<List<Artwork>, List<Episode>>

    suspend fun saveArtwork(artwork: Artwork)

    suspend fun saveEpisodes(episodes: List<Episode>)

}