package com.mskd.flux.data.repository.artwork

import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import kotlinx.coroutines.flow.Flow

interface ArtworkRepository {

    val flow: Flow<Content>

    fun searchArtwork(artworkId: Long)

    suspend fun saveMovie(movie: Movie)

    suspend fun saveEpisode(episode: Episode)

    suspend fun saveEpisodes(episodes: List<Episode>)

    suspend fun getArtwork(artworkId: Long) : Content

    sealed class Content {
        data class MOVIE(val artwork: Artwork, val movie: Movie) : Content()
        data class SHOW(val artwork: Artwork, val episodes: List<Episode>) : Content()
        data object ERROR : Content()
    }

}