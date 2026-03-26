package com.kaem.flux.data.repository.artwork

import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.flow.Flow

interface ArtworkRepository {

    val flow: Flow<State>

    fun searchArtwork(mediaId: Long)

    suspend fun saveMovie(movie: Movie)

    suspend fun saveEpisode(episode: Episode)

    suspend fun saveEpisodes(episodes: List<Episode>)

    data class State(
        val artwork: Artwork? = null,
        val movie: Movie? = null,
        val episodes: List<Episode> = emptyList()
    )

}