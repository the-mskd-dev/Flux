package com.kaem.flux.data.source.media

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Movie

interface MediaSource {

    data class Library(
        val overviews: List<MediaOverview> = emptyList(),
        val movies: List<Movie> = emptyList(),
        val episodes: List<Episode> = emptyList()
    )

    suspend fun getMedias(
        files: List<UserFile> = emptyList(),
        sync: Boolean
    ) : Library

}