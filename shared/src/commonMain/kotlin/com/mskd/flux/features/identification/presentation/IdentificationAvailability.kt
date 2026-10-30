package com.mskd.flux.features.identification.presentation

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.files.UserFile

/** Availability of the Identification manuelle option of the menu. */
enum class IdentificationAvailability {
    /** No TMDB token, a TMDB ID in a file name, the Unknown Artwork, or a page that does not offer it. */
    HIDDEN,
    /** During a Synchro, which could overwrite the choice. */
    DISABLED,
    ENABLED;

    companion object {

        /** A TMDB ID in the name of one of the [files] has priority over any Identification manuelle. */
        fun of(artwork: Artwork, files: List<UserFile>, tokenAvailable: Boolean, syncing: Boolean): IdentificationAvailability = when {
            artwork.isUnknown || !tokenAvailable -> HIDDEN
            files.any { it.nameProperties.tmdbId != null } -> HIDDEN
            syncing -> DISABLED
            else -> ENABLED
        }

    }

}
