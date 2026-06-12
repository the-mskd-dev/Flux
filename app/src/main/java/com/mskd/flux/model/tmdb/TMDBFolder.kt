package com.mskd.flux.model.tmdb

import com.mskd.flux.model.UserFile
import kotlinx.serialization.Serializable

@Serializable
data class TMDBFolder(
    val tmdbArtwork: TMDBArtwork,
    val files: List<UserFile>
)
