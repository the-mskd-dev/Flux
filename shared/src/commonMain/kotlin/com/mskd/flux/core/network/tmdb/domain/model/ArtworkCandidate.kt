package com.mskd.flux.core.network.tmdb.domain.model

import com.mskd.flux.core.model.artwork.Artwork

/**
 * A TMDB search result offered to the user for an Identification manuelle.
 *
 * @property artwork Artwork of the result, with its Poster.
 * @property year Release year (first air year for a Show), null when TMDB has no date.
 */
data class ArtworkCandidate(
    val artwork: Artwork,
    val year: Int?,
)
