package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.files.UserFile

data class ArtworkFiles(
    val artwork: Artwork,
    val files: List<UserFile>
)
