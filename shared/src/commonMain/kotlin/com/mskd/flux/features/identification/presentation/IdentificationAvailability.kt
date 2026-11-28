package com.mskd.flux.features.identification.presentation

import com.mskd.flux.core.model.artwork.Artwork

/** Availability of the Manual identification option of the menu. */
enum class IdentificationAvailability {
    /** No TMDB token, the Unknown Artwork, or a page that does not offer it. */
    HIDDEN,
    /** During a Sync, which could overwrite the choice. */
    DISABLED,
    ENABLED;

    companion object {

        /** Offered even when a file has a TMDB ID in its name: the Manual identification has priority over it. */
        fun of(artwork: Artwork, tokenAvailable: Boolean, syncing: Boolean): IdentificationAvailability =
            if (artwork.isUnknown) HIDDEN else of(tokenAvailable = tokenAvailable, syncing = syncing)

        fun of(tokenAvailable: Boolean, syncing: Boolean): IdentificationAvailability = when {
            !tokenAvailable -> HIDDEN
            syncing -> DISABLED
            else -> ENABLED
        }

    }

}
