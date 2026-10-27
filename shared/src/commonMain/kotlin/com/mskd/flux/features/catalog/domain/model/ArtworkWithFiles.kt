package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile

data class ArtworkWithFiles(
    val artwork: Artwork,
    val files: List<UserFile>
)

/** Outcome of the Artwork lookup of each folder: found (possibly [Artwork.UNKNOWN]) or failed. */
data class ArtworkFetchResult(
    val found: List<ArtworkWithFiles>,
    val failedFolders: List<CatalogFolder>,
)
