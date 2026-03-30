package com.mskd.flux.data.source.media

import com.mskd.flux.model.UserFile
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie

interface MediaSource {

    data class Library(
        val artworks: List<Artwork> = emptyList(),
        val movies: List<Movie> = emptyList(),
        val episodes: List<Episode> = emptyList()
    )

    suspend fun getMedias(files: List<UserFile> = emptyList()) : Library

}