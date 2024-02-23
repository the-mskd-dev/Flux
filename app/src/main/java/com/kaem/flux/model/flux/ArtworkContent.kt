package com.kaem.flux.model.flux

sealed class ArtworkContent {

    data class MOVIE(val movie: Movie) : ArtworkContent()

    data class SHOW(var episodes: List<Episode> = emptyList()) : ArtworkContent() {

        val currentEpisode get() = episodes.lastOrNull { it.status == FluxStatus.IS_WATCHING }
            ?: episodes.firstOrNull { it.status == FluxStatus.TO_WATCH }
            ?: episodes.firstOrNull()

    }

}