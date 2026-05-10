package com.mskd.flux.model.tmdb

import com.mskd.flux.model.UserFile

data class TMDBFolder(
    val tmdbArtwork: TMDBArtwork,
    val files: List<UserFile>
)
