package com.mskd.flux.features.artwork.ui

import com.mskd.flux.core.model.artwork.ArtworkPageType
import com.mskd.flux.core.model.artwork.FullArtwork

/** Kind of page the Artwork page currently is: a Movie page for a film, a Season page for a Season of a Show. */
internal val FullArtwork.pageType: ArtworkPageType get() = when (this) {
    is FullArtwork.FullMovie -> ArtworkPageType.MOVIE
    is FullArtwork.FullShow -> ArtworkPageType.SEASON
}
