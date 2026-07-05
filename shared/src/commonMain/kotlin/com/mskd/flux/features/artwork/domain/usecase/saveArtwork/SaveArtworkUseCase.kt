package com.mskd.flux.features.artwork.domain.usecase.saveArtwork

import com.mskd.flux.core.domain.model.artwork.Episode
import com.mskd.flux.core.domain.model.artwork.Movie

interface SaveArtworkUseCase {
    suspend fun saveMovie(movie: Movie)
    suspend fun saveEpisode(episode: Episode)
    suspend fun saveEpisodes(episodes: List<Episode>)
}