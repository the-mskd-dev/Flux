package com.mskd.flux.core.network.tmdb.domain.model

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Person

/**
 * TMDB details of an Artwork, fetched in a single call with its translations and credits.
 *
 * @property artwork Artwork with its title and description in the user language, completed by English.
 * @property movie Movie of the file for an Artwork of type movie, null for a show.
 * @property people Cast and crew of the Artwork.
 */
data class ArtworkDetails(
    val artwork: Artwork,
    val movie: Movie?,
    val people: List<Person>,
)
