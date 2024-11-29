package com.kaem.flux.model.flux

import com.google.gson.annotations.Expose
import com.kaem.flux.typeAdapters.ArtworkContentTypeAdapter

/**
 * Represents the content type of an artwork, which can be a movie or a TV show.
 * Serialized/Deserialized by [ArtworkContentTypeAdapter]
 *
 * @property MOVIE Represents a movie with its specific details.
 * @property SHOW Represents a TV show with its episodes.
 */
sealed class Content {

    /**
     * Represents movie-specific content.
     *
     * @property movie The movie associated with this content.
     */
    data class MOVIE(val movie: Movie) : Content()

    /**
     * Represents show-specific content.
     *
     * @property episodes List of episodes in the show.
     * @property currentEpisode Retrieves the episode currently being watched or the next episode to watch.
     */
    data class SHOW(
        @Expose(serialize = false) var episodes: List<Episode> = emptyList()
    ) : Content() {

        val currentEpisode get() = episodes.lastOrNull { it.status == Status.IS_WATCHING }
            ?: episodes.firstOrNull { it.status == Status.TO_WATCH }
            ?: episodes.firstOrNull()

    }

}