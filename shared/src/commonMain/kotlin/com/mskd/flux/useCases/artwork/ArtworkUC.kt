package com.mskd.flux.useCases.artwork

import com.mskd.flux.model.core.presentation.State
import com.mskd.flux.model.domain.artwork.Episode
import com.mskd.flux.model.domain.artwork.FullArtwork
import com.mskd.flux.model.domain.artwork.Movie
import kotlinx.coroutines.flow.Flow

interface ArtworkUC {

    val flow: Flow<State<FullArtwork>>

    fun searchArtwork(artworkId: Long)

    suspend fun saveMovie(movie: Movie)

    suspend fun saveEpisode(episode: Episode)

    suspend fun saveEpisodes(episodes: List<Episode>)

    suspend fun getArtwork(artworkId: Long) : FullArtwork?

}