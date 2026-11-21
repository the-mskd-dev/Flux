package com.mskd.flux.navigation.domain

import com.mskd.flux.core.model.artwork.ContentType

/** The page of the Artwork [artworkId], from its [type]: the Movie page for a MOVIE, the Show page for a SHOW. */
fun artworkPage(artworkId: Long, type: ContentType, rgb: Int?): Route = when (type) {
    ContentType.MOVIE -> Route.Movie(artworkId = artworkId, rgb = rgb)
    ContentType.SHOW -> Route.Show(artworkId = artworkId, rgb = rgb)
}

/**
 * The page reached after a Manual identification, from the [type] of the new Artwork [artworkId]: see [artworkPage].
 * The colour of the previous page is dropped: it belonged to the previous Artwork.
 */
fun identifiedDestination(artworkId: Long, type: ContentType): Route = artworkPage(artworkId = artworkId, type = type, rgb = null)
