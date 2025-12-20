package com.kaem.flux.data.source.media

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.Movie

interface MediaSource {

    data class Library(
        val artworks: List<Artwork> = emptyList(),
        val movies: List<Movie> = emptyList(),
        val episodes: List<Episode> = emptyList()
    )

    suspend fun getMedias(files: List<UserFile> = emptyList()) : Library

}