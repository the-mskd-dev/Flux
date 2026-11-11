package com.mskd.flux.navigation.domain

import com.mskd.flux.core.model.artwork.ContentType

/**
 * The page reached after a Manual identification, from the [type] of the new Artwork [artworkId]: the movie page for a
 * Movie, the Show page for a Show. The season of the previous page is dropped, and so is its colour, which belonged to
 * the previous Artwork.
 */
fun identifiedDestination(artworkId: Long, type: ContentType): Route = when (type) {
    ContentType.MOVIE -> Route.Artwork(artworkId = artworkId, season = null, rgb = null)
    ContentType.SHOW -> Route.Show(artworkId = artworkId, rgb = null)
}
