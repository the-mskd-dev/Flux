package com.kaem.flux.data.source.artwork

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie

interface ArtworkDataSource {

    data class Library(
        val artworks: List<Artwork> = emptyList(),
        val movies: List<Movie> = emptyList(),
        val episodes: List<Episode> = emptyList()
    )

    suspend fun getArtworks(
        files: List<UserFile> = emptyList(),
        artworkIds: List<Long> = emptyList(),
        sync: Boolean
    ) : Library

}