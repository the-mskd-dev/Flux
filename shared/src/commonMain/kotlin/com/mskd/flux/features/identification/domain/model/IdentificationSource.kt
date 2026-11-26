package com.mskd.flux.features.identification.domain.model

import com.mskd.flux.core.model.artwork.Episode

/** Source of a Manual identification: either a Catalog Artwork or an Unrecognized file. */
sealed interface IdentificationSource {

    /** Unique key identifying this source, used for ViewModel scoping. */
    val key: String

    data class KnownArtwork(val artworkId: Long) : IdentificationSource {
        override val key: String get() = "artwork_$artworkId"
    }

    data class UnknownMedia(val media: Episode) : IdentificationSource {
        override val key: String get() = "unknown_${media.id}"
    }

}
