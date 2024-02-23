package com.kaem.flux.model.flux

import com.google.gson.annotations.Expose
import com.kaem.flux.typeAdapters.ArtworkContentTypeAdapter

/**
 * Serialized/Deserialized by [ArtworkContentTypeAdapter]
 */
sealed class ArtworkContent {

    data class MOVIE(val movie: Movie) : ArtworkContent()

    data class SHOW(
        @Expose(serialize = false) var episodes: List<Episode> = emptyList()
    ) : ArtworkContent() {

        val currentEpisode get() = episodes.lastOrNull { it.status == FluxStatus.IS_WATCHING }
            ?: episodes.firstOrNull { it.status == FluxStatus.TO_WATCH }
            ?: episodes.firstOrNull()

    }

}